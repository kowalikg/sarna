#include <jni.h>
#include <string>
#include <unistd.h>
#include <string.h>

using namespace std;
extern "C" JNIEXPORT jstring

JNICALL
Java_pl_edu_agh_sarna_WifiPasswordActivity_getWifiPassword(
        JNIEnv *env,
        jobject) {

    return env->NewStringUTF("KOKO");
}

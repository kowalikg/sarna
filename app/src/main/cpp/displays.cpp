#include <jni.h>
#include <string>
using namespace std;

extern "C"
JNIEXPORT jstring JNICALL

Java_pl_edu_agh_sarna_MainActivity_displayMessage(JNIEnv *env, jobject instance,
                                                  jboolean privilleges) {

    string message = privilleges ? "Welcome to sarna app!" : "You have no root privilleges!";
    return env->NewStringUTF(message.c_str());

}
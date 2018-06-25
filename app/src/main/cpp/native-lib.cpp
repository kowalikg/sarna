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
    string name = "/data/misc/wifi/wpa_supplicant.conf";
    string command = "su -c \"chmod 0777 ";
    command += name;
    command += "\"";

    FILE *pCmd = NULL;
    char strCmd[1024] = "su -c \"cat ";
    strcat(strCmd, name.c_str());
    strcat(strCmd, "\"");

//use popen to read shell output
    pCmd = popen(strCmd, "r");
    if(!pCmd)
    {
        return false;
    }

//here you can read pCmd using fread, fgets, fgetc:
    char token = fgetc(pCmd);

//when finish reading
    pclose(pCmd);


    return env->NewStringUTF("KOKO");
}

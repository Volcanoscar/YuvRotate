#include <jni.h>
#include <string>

#include <android/asset_manager.h>
#include <android/asset_manager_jni.h>

#include "system.h"

void setAssetManager(AAssetManager* pManager);

extern "C" JNIEXPORT jstring JNICALL
Java_com_volcanoscar_yuvutil_YuvUtilsJni_stringFromJNI(
        JNIEnv *env,
        jclass type) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_com_volcanoscar_yuvutil_YuvUtilsJni_createAssetManager(JNIEnv *env, jclass type, jobject assetManager) {
    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    assert(mgr);
    setAssetManager(mgr);
}

extern "C" JNIEXPORT void JNICALL
Java_com_volcanoscar_yuvutil_YuvUtilsJni_rotateYuv(JNIEnv *env, jclass type, jint rotation, jint width, jint height) {
    LOGE("Rotate enter..");

    char* file_data = NULL;
    unsigned int position = 0;
    unsigned int size = width * height;
    unsigned int half_size = size >> 1;
    unsigned int half_width = width >> 1;
    unsigned int half_height = height >> 1;
    read_file(ORIGINAL_YUV_FILENAME, &file_data);

    char* uv_date = file_data + size;

    char* dst_data = (char *)(malloc(size * 3 / 2));

    LOGE("Rotate start..");

    switch (rotation) {
        case 90:
            for (int i = 0; i < width; i++) {
                for (int j = height - 1; j >= 0; j--) {
                    dst_data[position++] = file_data[j * width + i];
                }
            }

            //rotate uv panel
            for (int m = 0; m < half_width; m++) {
                for (int n = half_height - 1; n >= 0; n--) {
                    dst_data[position++] = uv_date[n * width + 2 * m];
                    dst_data[position++] = uv_date[n * width + 2 * m + 1];
                }
            }

            break;

        case 180:
            for (int i = size - 1; i >= 0; i--) {
                dst_data[position++] = file_data[i];
            }

            //rotate uv panel
            for (int j = half_size - 2; j >= 0; j -= 2) {
                dst_data[position++] = uv_date[j];
                dst_data[position++] = uv_date[j + 1];
            }

            break;

        case 270:
            for (int i = width - 1; i >= 0; i--) {
                for (int j = 0; j < height; j++) {
                    dst_data[position++] = file_data[j * width + i];
                }
            }

            //rotate uv panel
            for (int m = half_width - 1; m >= 0; m--) {
                for (int n = 0; n < half_height; n++) {
                    dst_data[position++] = uv_date[n * width + 2 * m];
                    dst_data[position++] = uv_date[n * width + 2 * m + 1];
                }
            }

            break;

        default:
            memcpy(dst_data, file_data, (size + half_size));
            break;
    }

    LOGE("Rotate end..");

    save_file(OUTPUT_YUV_FILENAME, &dst_data);
    free(dst_data);
    free(file_data);
    uv_date = NULL;
    file_data = NULL;
    dst_data = NULL;

    LOGE("Rotate exit..");
}
extern "C" JNIEXPORT void JNICALL
Java_com_volcanoscar_yuvutil_YuvUtilsJni_releaseAssetManager(JNIEnv *env, jclass type) {
    setAssetManager(NULL);
}
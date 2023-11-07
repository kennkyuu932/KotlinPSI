#include <jni.h>
#include <string>
#include <android/log.h>
#include "include/openssl/ssl.h"

//EC_KEY *ec_key_psi;

extern "C" JNIEXPORT jstring JNICALL
Java_com_example_kotlinpsi_MainActivity_stringFromJNI(
        JNIEnv* env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
extern "C"
JNIEXPORT jint JNICALL
Java_com_example_kotlinpsi_MainActivity_Boringtest(JNIEnv *env, jobject thiz) {
    int nid = EC_curve_nist2nid("P-256");
    return nid;
}


bool ec_point_to_binary(const EC_GROUP *group, const EC_POINT *point, uint8_t **out, size_t *out_len, BN_CTX *ctx) {
    size_t len = EC_POINT_point2oct(group, point, POINT_CONVERSION_UNCOMPRESSED, nullptr, 0, ctx);
    *out = new uint8_t [len];
    if(!EC_POINT_point2oct(group, point, POINT_CONVERSION_UNCOMPRESSED, *out, len, ctx))
        return false;
    *out_len = len;
    return true;
}

EC_POINT *binary_to_ec_point(const EC_GROUP *group, const uint8_t *binaryData, size_t binaryDataLen, BN_CTX *ctx) {
    EC_POINT *point = EC_POINT_new(group);
    // バイナリデータをEC_POINT型に変換
    if (EC_POINT_oct2point(group, point, binaryData, binaryDataLen, ctx) != 1) {
        // 変換に失敗した場合のエラーハンドリングを行う
        EC_POINT_free(point);
        __android_log_print(ANDROID_LOG_DEBUG,"cpp","binary to ecpoint false");
        return nullptr;
    }
    return point;
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_kotlinpsi_MainActivity_OneCryptoMessage(JNIEnv *env, jobject thiz, jstring message,jstring message_cl) {
    const char *mes=env->GetStringUTFChars(message,nullptr);
    EC_KEY *ec_key = EC_KEY_new_by_curve_name(EC_curve_nist2nid("P-256"));
    EC_KEY_generate_key(ec_key);
    const EC_GROUP *ec_group = EC_KEY_get0_group(ec_key);
    //EC_POINT *pub_key_point = EC_POINT_new(ec_group);
    //EC_POINT *pri_key_point = EC_POINT_new(ec_group);
    //EC_POINT_copy(pub_key_point, EC_KEY_get0_public_key(ec_key));
    const BIGNUM *pri_key_point = EC_KEY_get0_private_key(ec_key);
    /*
     * 鍵:pri_key_point mesをpri_key_pointで暗号化
     */
    //
    EC_POINT *ps= EC_POINT_new(ec_group);
    EC_POINT *inf= EC_POINT_new(ec_group);
    int r = EC_POINT_set_to_infinity(ec_group,inf);
    BN_CTX *ctx = BN_CTX_new();
    BIGNUM zero;
    BN_init(&zero);
    BN_zero(&zero);
    int nx= strlen(mes);
    EC_POINT *px[nx];
    BIGNUM x[nx];
    int i;
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","set message to BIGNUM");
    for (i=0;i<nx;i++){
        BN_init(&x[i]);
        BN_set_word(&x[i],(BN_ULONG)mes[i]);
    }
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","crypto message use pri_key_point");
    r= EC_POINT_mul(ec_group,ps,pri_key_point,inf,&zero,ctx);
    unsigned char *px_binary[nx];
    size_t binary_len[nx];
    for(i=0;i<nx;i++){
        px[i]= EC_POINT_new(ec_group);
        r= EC_POINT_mul(ec_group,px[i],
                        &zero,ps,&x[i],
                        ctx);
        //バイナリデータに変換
        ec_point_to_binary(ec_group,px[i],&px_binary[i],&binary_len[i],ctx);
    }

    //バイナリデータをEC_POINTに変換
    EC_POINT *px_binary_ec[nx];
    for(i=0;i<nx;i++){
        px_binary_ec[i]= binary_to_ec_point(ec_group,px_binary[i],binary_len[i],ctx);
    }
    //


    //クライアント側の動作
    const char *mes_cl=env->GetStringUTFChars(message_cl, nullptr);
    EC_KEY *ec_key_cl = EC_KEY_new_by_curve_name(EC_curve_nist2nid("P-256"));
    EC_KEY_generate_key(ec_key_cl);
    const EC_GROUP *ec_group_cl = EC_KEY_get0_group(ec_key_cl);
    //EC_POINT *pub_key_point_cl = EC_POINT_new(ec_group_cl);
    //EC_POINT *pri_key_point_cl = EC_POINT_new(ec_group_cl);
    //EC_POINT_copy(pub_key_point_cl, EC_KEY_get0_public_key(ec_key_cl));
    const BIGNUM *pri_key_point_cl = EC_KEY_get0_private_key(ec_key_cl);
    /*
     * 鍵:pri_key_point_cl mes_clをpri_key_point_clで暗号化
     */
    //
    EC_POINT *ps_cl= EC_POINT_new(ec_group_cl);
    EC_POINT *inf_cl= EC_POINT_new(ec_group_cl);
    int r_cl = EC_POINT_set_to_infinity(ec_group_cl,inf_cl);
    BN_CTX *ctx_cl = BN_CTX_new();
    BIGNUM zero_cl;
    BN_init(&zero_cl);
    BN_zero(&zero_cl);
    int nx_cl= strlen(mes_cl);
    EC_POINT *px_cl[nx_cl];
    BIGNUM x_cl[nx_cl];
    int i_cl;
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","set message to BIGNUM client");
    for (i_cl=0;i_cl<nx_cl;i_cl++){
        BN_init(&x_cl[i_cl]);
        BN_set_word(&x_cl[i_cl],(BN_ULONG)mes_cl[i_cl]);
    }
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","crypto message use pri_key_point client");
    r_cl= EC_POINT_mul(ec_group_cl,ps_cl,pri_key_point_cl,inf_cl,&zero_cl,ctx_cl);
    for(i_cl=0;i_cl<nx_cl;i_cl++){
        px_cl[i_cl]= EC_POINT_new(ec_group_cl);
        r_cl= EC_POINT_mul(ec_group_cl,px_cl[i_cl],
                           &zero_cl,ps_cl,&x_cl[i_cl],
                           ctx_cl);
    }

    //クライアントからサーバにpx_cl[]が送られてきた想定
    //px_cl[]をpri_key_pointで暗号化
    EC_POINT *pxy[nx_cl];
    for(i=0;i<nx_cl;i++){
        pxy[i]= EC_POINT_new(ec_group);
        r= EC_POINT_mul(ec_group,pxy[i],
                        &zero,px_cl[i],pri_key_point,
                        ctx);
    }

    //サーバからpxyが送られてきた想定
    //pxy[]に対してpri_key_point_clで復号
    //pri_key_point_clの逆元を求めて掛け算
    EC_POINT *pxy_enc[nx_cl];
    BIGNUM cl_key_inverse;
    int out_no_inverse;
    BN_CTX *bnCtx;
    BN_MONT_CTX *bnMontCtx;
    BN_init(&cl_key_inverse);
    bnCtx=BN_CTX_new();
    bnMontCtx= BN_MONT_CTX_new_for_modulus(EC_GROUP_get0_order(ec_group_cl),bnCtx);
    r_cl= BN_mod_inverse_blinded(&cl_key_inverse,&out_no_inverse,pri_key_point_cl,bnMontCtx,bnCtx);
    for(i_cl=0;i_cl<nx_cl;i_cl++){
        pxy_enc[i_cl]= EC_POINT_new(ec_group_cl);
        r= EC_POINT_mul(ec_group_cl, pxy_enc[i_cl],
                        &zero_cl, pxy[i_cl], &cl_key_inverse,
                        ctx_cl);
    }

    //共通要素を調べる
    //pxとpxy_encを比較
    i=i_cl=0;
    int k=-1;
    std::string result="";
//    for(i=0;i<nx;i++){
//        for(i_cl=0;i_cl<nx_cl;i_cl++){
//            if(EC_POINT_cmp(ec_group_cl,px[i],pxy_enc[i_cl],ctx_cl)==0){
//                //共通要素
//                k=i_cl;
//                __android_log_print(ANDROID_LOG_DEBUG,"cpp","psi: %c",mes_cl[i_cl]);
//                break;
//            }
//        }
//    }
// 二重ループの削除
/*
 * 上記の二重ループにすると"test"と"teat"を比較したときに"test"の2回目の"t"と"teat"の最初の"t"が共通要素になってしまう
 */
    for(i_cl=0;i_cl<nx_cl;i_cl++){
        if(EC_POINT_cmp(ec_group_cl,px_binary_ec[i_cl],pxy_enc[i_cl],ctx_cl)==0){
            __android_log_print(ANDROID_LOG_DEBUG,"cpp","psi: %c",mes_cl[i_cl]);
            result+=mes_cl[i_cl];
        }
    }


    //メモリ開放
//    BN_free(pri_key_point);
//    BN_free(pri_key_point_cl);
    BN_free(&zero);
    BN_free(&zero_cl);
    BN_free(&cl_key_inverse);
    for(i=0;i<nx;i++){
        BN_free(&x[i]);
        BN_free(&x_cl[i]);
        EC_POINT_free(px[i]);
        EC_POINT_free(px_cl[i]);
        EC_POINT_free(pxy[i]);
        EC_POINT_free(pxy_enc[i]);
    }
    EC_KEY_free(ec_key);
    EC_KEY_free(ec_key_cl);
//    EC_GROUP_free(ec_group);
//    EC_GROUP_free(ec_group_cl);

    EC_POINT_free(ps);
    EC_POINT_free(inf);
    EC_POINT_free(ps_cl);
    EC_POINT_free(inf_cl);

    BN_CTX_free(ctx);
    BN_CTX_free(ctx_cl);
    BN_CTX_free(bnCtx);

    BN_MONT_CTX_free(bnMontCtx);

    env->ReleaseStringUTFChars(message,mes);
    env->ReleaseStringUTFChars(message_cl,mes_cl);

    return env->NewStringUTF(result.c_str());
}


jbyteArray Char_to_bytearray(JNIEnv *env,unsigned char* buf,int len){
    jbyteArray array=env->NewByteArray(len);
    env->SetByteArrayRegion(array,0,len,reinterpret_cast<jbyte*>(buf));
    return array;
}



extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_kotlinpsi_Transmission_ServerActivity_createKey(JNIEnv *env, jobject thiz,
                                                                 jbyteArray key) {
    EC_KEY *ec_key = EC_KEY_new_by_curve_name(EC_curve_nist2nid("P-256"));
    EC_KEY_generate_key(ec_key);
    //const EC_GROUP *ec_group = EC_KEY_get0_group(ec_key);
    const BIGNUM *pri_key_bn = EC_KEY_get0_private_key(ec_key);
    size_t len = BN_num_bytes(pri_key_bn);
    uint8_t pri_key_byte[len];
    int bin2bn_flag=BN_bn2bin_padded(pri_key_byte,len,pri_key_bn);
    if(!bin2bn_flag){
        return false;
    }
    jsize len_j = env->GetArrayLength(key);
    env->SetByteArrayRegion(key,0,len_j,(jbyte *)pri_key_byte);
    return true;
}

extern "C"
JNIEXPORT jboolean JNICALL
Java_com_example_kotlinpsi_Transmission_ServerActivity_encryptSet(JNIEnv *env, jobject thiz,
                                                                  jstring message, jbyteArray key,
                                                                  jbyteArray out) {
    const char *mes=env->GetStringUTFChars(message,nullptr);
    int len=(int) env->GetArrayLength(key);
    uint8_t pri_key_byte[len];
    env->GetByteArrayRegion(key,0,len,(jbyte *)pri_key_byte);
    BIGNUM *pri_key;
//    BN_bin2bn(pri_key_byte,len,pri_key);
//    if(BN_bin2bn(pri_key_byte,len,pri_key)==NULL){
//        __android_log_print(ANDROID_LOG_DEBUG,"cpp","exchange byte to BIGNUM failed");
//        return false;
//    }
    pri_key = BN_bin2bn(pri_key_byte,len, nullptr);
    if(pri_key==NULL){
        __android_log_print(ANDROID_LOG_DEBUG,"cpp","private key is NULL");
    }
    EC_GROUP *ec_group= EC_GROUP_new_by_curve_name(EC_curve_nist2nid("P-256"));
    EC_POINT *ps= EC_POINT_new(ec_group);
    EC_POINT *inf= EC_POINT_new(ec_group);
    int r = EC_POINT_set_to_infinity(ec_group,inf);
    BN_CTX *ctx = BN_CTX_new();
    BIGNUM zero;
    BN_init(&zero);
    BN_zero(&zero);
//    int nx= strlen(mes);
//    int nx = 1; //mesの文字列の数(現状では"test"の一単語)
    EC_POINT *px;
    BIGNUM x;
    int i;
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","set message to BIGNUM");
    BN_init(&x);
    BN_set_word(&x,(BN_ULONG)mes);
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","crypto message use pri_key_point");
    //ps=generator*pri_key+inf*0=generator*pri_key?
    r= EC_POINT_mul(ec_group,ps,pri_key,inf,&zero,ctx);
    uint8_t *px_binary;
    size_t binary_len;
    px= EC_POINT_new(ec_group);
    r= EC_POINT_mul(ec_group,px,
                    &zero,ps,&x,
                    ctx); //px[i]=generator*0+ps*x[i]=ps*x[i] ps:privatekey(EC_POINT),x[i]:message(BIGNUM)
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","encrypt message ");
    //バイナリデータに変換
    if(!ec_point_to_binary(ec_group,px,&px_binary,&binary_len,ctx)) {
        __android_log_print(ANDROID_LOG_DEBUG,"cpp","ecpoint to binary false");
        return false;
    }
    __android_log_print(ANDROID_LOG_DEBUG,"cpp","encrypt and exchange ecpoint to byte");
    env->SetByteArrayRegion(out,0,binary_len,(jbyte *)px_binary);
    //バイナリデータをEC_POINTに変換
    EC_POINT *px_binary_ec;
    px_binary_ec= binary_to_ec_point(ec_group,px_binary,binary_len,ctx);


    //逆元の計算
    EC_POINT *pxy_enc;
    EC_POINT *px_reenc;
    BIGNUM cl_key_inverse;
    int out_no_inverse;
    BN_CTX *bnCtx;
    BN_MONT_CTX *bnMontCtx;
    BN_init(&cl_key_inverse);
    bnCtx=BN_CTX_new();
    bnMontCtx= BN_MONT_CTX_new_for_modulus(EC_GROUP_get0_order(ec_group),bnCtx);
    r= BN_mod_inverse_blinded(&cl_key_inverse,&out_no_inverse,pri_key,bnMontCtx,bnCtx);
    int ec_cmp_flag;
    pxy_enc= EC_POINT_new(ec_group);
    r= EC_POINT_mul(ec_group, pxy_enc,
                    &zero, px, &cl_key_inverse,
                    ctx);
    //pxy_enc=generator*0+px*cl_key_inverse=px*cl_key_inverse
    //px=pxy_enc/cl_key_inverse=pxy_enc*pri_key
//    r= EC_POINT_mul(ec_group,px_reenc,&zero,pxy_enc,pri_key,bnCtx);
//    ec_cmp_flag=EC_POINT_cmp(ec_group,px_reenc,px,bnCtx);
//    if(ec_cmp_flag==0){
//        __android_log_print(ANDROID_LOG_DEBUG,"cpp","compare true");
//    }

    return true;
}
package com.example.smarttasksapp.feature.input;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.ocr_api20210707.models.*;
import com.aliyun.sdk.service.ocr_api20210707.AsyncClient;
import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;

import com.example.smarttasksapp.feature.input.OcrResult;

import darabonba.core.client.ClientOverrideConfiguration;

/**
 * 阿里云OCR管理类
 * 用于处理OCR初始化和文字识别功能
 */
public class AliyunOcrManager {
    private static final String TAG = "AliyunOcrManager";

    private AsyncClient client;
    private Context context;

    // 阿里云AccessKey ID和AccessKey Secret
    // 请在使用时替换为实际的密钥
    private String accessKeyId = "your";
    private String accessKeySecret = "your";

    public AliyunOcrManager(Context context) {
        this.context = context;
        initializeClient();
    }

    /**
     * 初始化阿里云OCR客户端
     */
    private void initializeClient() {
        try {
            // Configure Credentials authentication information, including ak, secret, token
            StaticCredentialProvider provider = StaticCredentialProvider.create(Credential.builder()
                    .accessKeyId(accessKeyId)
                    .accessKeySecret(accessKeySecret)
                    .build());

            // Configure the Client
            client = AsyncClient.builder()
                    .credentialsProvider(provider)
                    .overrideConfiguration(
                            ClientOverrideConfiguration.create()
                                    .setEndpointOverride("ocr-api.cn-hangzhou.aliyuncs.com")
                    )
                    .build();
            Log.d(TAG, "阿里云OCR客户端初始化成功");
        } catch (Exception e) {
            Log.e(TAG, "阿里云OCR客户端初始化失败", e);
        }
    }

    /**
     * 识别图片中的文字
     *
     * @param bitmap 图片Bitmap
     * @return 识别结果的CompletableFuture对象
     */
    public CompletableFuture<OcrResult> recognizeText(Bitmap bitmap) {
        // 创建一个在后台线程中执行的CompletableFuture
        return CompletableFuture.supplyAsync(() -> {
            try {
                // 将Bitmap转换为字节数组
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] imageBytes = stream.toByteArray();

                // 将字节数组转换为InputStream
                ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);

                // 参数设置
                RecognizeBasicRequest request = RecognizeBasicRequest.builder()
                        .body(inputStream)
                        .needRotate(true)
                        .build();

                // 异步调用API
                CompletableFuture<RecognizeBasicResponse> response = client.recognizeBasic(request);
                
                // 等待响应完成并返回结果
                RecognizeBasicResponse recognizeBasicResponse = response.get();
                
                if (recognizeBasicResponse != null && recognizeBasicResponse.getBody() != null) {
                    // 获取响应数据
                    String jsonData = recognizeBasicResponse.getBody().getData();
                    // 使用Gson将JSON字符串解析为OcrResult对象
                    OcrResult ocrResult = new Gson().fromJson(jsonData, OcrResult.class);
                    return ocrResult;
                } else {
                    Log.e(TAG, "OCR识别返回空结果");
                    // 创建一个包含错误信息的OcrResult对象
                    OcrResult ocrResult = new OcrResult();
                    ocrResult.setContent("识别失败：返回结果为空");
                    return ocrResult;
                }
            } catch (Exception e) {
                Log.e(TAG, "OCR识别过程中发生异常", e);
                // 检查是否为OCR服务未激活的错误
                String errorMessage = e.getMessage();
                OcrResult errorResult = new OcrResult();
                
                if (errorMessage != null && errorMessage.contains("ocrServiceNotOpen")) {
                    errorResult.setContent("识别失败：阿里云OCR服务尚未激活。请登录阿里云控制台激活OCR服务，并确保AccessKey对应的账号有使用OCR服务的权限。");
                } else if (errorMessage != null && errorMessage.contains("401")) {
                    errorResult.setContent("识别失败：认证失败，请检查AccessKey ID和AccessKey Secret是否正确。");
                } else {
                    errorResult.setContent("识别失败：" + errorMessage);
                }
                
                return errorResult;
            }
        });
    }

    /**
     * 设置阿里云AccessKey ID和AccessKey Secret
     * @param accessKeyId AccessKey ID
     * @param accessKeySecret AccessKey Secret
     */
    public void setCredentials(String accessKeyId, String accessKeySecret) {
        this.accessKeyId = accessKeyId;
        this.accessKeySecret = accessKeySecret;
        initializeClient(); // 重新初始化客户端
    }
}
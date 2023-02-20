package com.lujiatao.mock.module.util;

import com.lujiatao.mock.module.constant.HttpMethod;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.NameValuePair;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.apache.pdfbox.util.Charsets.UTF_8;

/**
 * HTTP工具
 *
 * @author 卢家涛
 */
public class HttpUtil implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);

    private final CloseableHttpClient client;

    private HttpUriRequestBase request;

    private CloseableHttpResponse response;

    private HttpMethod method;

    private String url;

    // 基URL
    private String baseUrl;

    // 接口路径
    private String path;

    // 查询参数
    private Map<String, String> queryParams;

    // 普通表单参数，Content-Type为application/x-www-form-urlencoded。
    private Map<String, String> formParams;

    // 上传表单参数，Content-Type为multipart/form-data。
    private Map<String, Object> uploadFormParams;

    // JSON参数，Content-Type为application/json。
    private String jsonParams;

    private Map<String, String> headers;

    public static class HttpUtilBuilder {

        private HttpMethod method = HttpMethod.GET;

        private int requestTimeout;

        private TimeUnit timeUnit;

        private String url;

        private String baseUrl;

        private String path;

        private Map<String, String> queryParams;

        private Map<String, String> formParams;

        private Map<String, Object> uploadFormParams;

        private String jsonParams;

        private Map<String, String> headers;

        private final boolean redirect;

        public HttpUtilBuilder(String url) {
            this(url, true);
        }

        public HttpUtilBuilder(String url, boolean redirect) {
            // 如果是HTTP，index为6（HTTP://）；如果是HTTPS，index为7（HTTPS://）。因此index大于7时还包含“/”说明是完整URL，而不是基URL。
            if (url.substring(8).contains("/")) {
                this.url = url;
            } else {
                this.baseUrl = url;
            }
            this.redirect = redirect;
        }

        public HttpUtilBuilder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public HttpUtilBuilder requestTimeout(int requestTimeout, TimeUnit timeUnit) {
            this.requestTimeout = requestTimeout;
            this.timeUnit = timeUnit;
            return this;
        }

        public HttpUtilBuilder path(String path) {
            this.path = path;
            return this;
        }

        public HttpUtilBuilder queryParams(Map<String, String> queryParams) {
            this.queryParams = queryParams;
            return this;
        }

        public HttpUtilBuilder formParams(Map<String, String> formParams) {
            this.formParams = formParams;
            return this;
        }

        public HttpUtilBuilder uploadFormParams(Map<String, Object> uploadFormParams) {
            this.uploadFormParams = uploadFormParams;
            return this;
        }

        public HttpUtilBuilder jsonParams(String jsonParams) {
            this.jsonParams = jsonParams;
            return this;
        }

        public HttpUtilBuilder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public HttpUtil build() {
            return new HttpUtil(this, this.redirect);
        }

    }

    private HttpUtil(HttpUtilBuilder httpUtilBuilder, boolean redirect) {
        this.method = httpUtilBuilder.method;
        int requestTimeout = httpUtilBuilder.requestTimeout;
        TimeUnit timeUnit = httpUtilBuilder.timeUnit;
        this.baseUrl = httpUtilBuilder.baseUrl;
        this.path = httpUtilBuilder.path;
        this.url = httpUtilBuilder.url;
        if (this.url == null) {
            if (this.path == null) {
                this.path = "/";
            }
            this.url = this.baseUrl + this.path;
        }
        this.queryParams = httpUtilBuilder.queryParams;
        this.formParams = httpUtilBuilder.formParams;
        this.uploadFormParams = httpUtilBuilder.uploadFormParams;
        this.jsonParams = httpUtilBuilder.jsonParams;
        this.headers = httpUtilBuilder.headers;
        // 创建HTTP客户端
        this.client = redirect ? HttpClients.createDefault() : HttpClients.custom().disableRedirectHandling().build();
        this.setMethod(this.method)
                .setRequestTimeout(requestTimeout, timeUnit)
                .setQueryParams(this.queryParams)
                .setFormParams(this.formParams)
                .setUploadFormParams(this.uploadFormParams)
                .setJsonParams(this.jsonParams)
                .setHeaders(this.headers);
    }

    /**
     * 设置HTTP请求方法
     *
     * @param method HTTP请求方法
     * @return HttpUtil
     */
    public HttpUtil setMethod(HttpMethod method) {
        Map<String, String> tmp = this.headers;
        this.method = method;
        switch (this.method) {
            case GET:
                this.request = new HttpGet(this.url);
                break;
            case POST:
                this.request = new HttpPost(this.url);
                break;
            case PUT:
                this.request = new HttpPut(this.url);
                break;
            case PATCH:
                this.request = new HttpPatch(this.url);
                break;
            case DELETE:
                this.request = new HttpDelete(this.url);
                break;
            case HEAD:
                this.request = new HttpHead(this.url);
                break;
            case OPTIONS:
                this.request = new HttpOptions(this.url);
                break;
            case TRACE:
                this.request = new HttpTrace(this.url);
                break;
            default:
                logger.error("HTTP请求方法 {} 不受支持。", this.method);
        }
        // 保留Header
        this.setHeaders(tmp);
        return this;
    }

    /**
     * 设置HTTP请求超时时间
     *
     * @param num      HTTP请求超时时间，传0则不限制超时时间。
     * @param timeUnit HTTP请求超时单位
     * @return HttpUtil
     */
    public HttpUtil setRequestTimeout(int num, TimeUnit timeUnit) {
        if (num != 0) {
            RequestConfig requestConfig = RequestConfig.custom().setResponseTimeout(num, timeUnit).build();
            this.request.setConfig(requestConfig);
        }
        return this;
    }

    /**
     * 设置URL
     *
     * @param url URL
     * @return HttpUtil
     */
    public HttpUtil setUrl(String url) {
        if (url != null) {
            this.url = url;
            try {
                this.request.setUri(new URIBuilder(this.url).build());
            } catch (URISyntaxException e) {
                logger.error("URL {} 语法错误。", this.url);
            }
        }
        return this;
    }

    /**
     * 设置基URL
     *
     * @param baseUrl 基URL
     * @return HttpUtil
     */
    public HttpUtil setBaseUrl(String baseUrl) {
        if (baseUrl != null) {
            this.baseUrl = baseUrl;
            this.url = this.baseUrl + this.path;
            try {
                this.request.setUri(new URIBuilder(this.url).build());
            } catch (URISyntaxException e) {
                logger.error("URL {} 语法错误。", this.baseUrl + this.path);
            }
        }
        return this;
    }

    /**
     * 设置接口路径
     *
     * @param path 接口路径
     * @return HttpUtil
     */
    public HttpUtil setPath(String path) {
        if (path != null) {
            this.path = path;
            this.url = this.baseUrl + this.path;
            try {
                this.request.setUri(new URIBuilder(this.url).build());
            } catch (URISyntaxException e) {
                logger.error("URL {} 语法错误。", this.baseUrl + this.path);
            }
        }
        return this;
    }

    /**
     * 设置查询参数（单个）
     * /**
     *
     * @param key   查询参数名称
     * @param value 查询参数值
     * @return HttpUtil
     */
    public HttpUtil setQueryParam(String key, String value) {
        if (this.queryParams == null) {
            this.queryParams = new HashMap<>();
        }
        this.queryParams.put(key, value);
        try {
            URIBuilder builder = new URIBuilder(this.url);
            for (Map.Entry<String, String> entry : this.queryParams.entrySet()) {
                builder.setParameter(entry.getKey(), entry.getValue());
            }
            this.request.setUri(builder.build());
        } catch (URISyntaxException e) {
            logger.error("URL {} 语法错误。", this.url);
        }
        return this;
    }

    /**
     * 设置查询参数（批量）
     *
     * @param queryParams 查询参数
     * @return HttpUtil
     */
    public HttpUtil setQueryParams(Map<String, String> queryParams) {
        if (queryParams != null) {
            this.queryParams = queryParams;
            try {
                URIBuilder builder = new URIBuilder(this.url);
                for (Map.Entry<String, String> entry : this.queryParams.entrySet()) {
                    builder.setParameter(entry.getKey(), entry.getValue());
                }
                this.request.setUri(builder.build());
            } catch (URISyntaxException e) {
                logger.error("URL {} 语法错误。", this.url);
            }
        }
        return this;
    }

    /**
     * 设置普通表单参数（单个）
     *
     * @param key   普通表单参数名称
     * @param value 普通表单参数值
     * @return HttpUtil
     */
    public HttpUtil setFormParam(String key, String value) {
        if (this.formParams == null) {
            this.formParams = new HashMap<>();
        }
        this.formParams.put(key, value);
        List<NameValuePair> params = new ArrayList<>();
        for (Map.Entry<String, String> entry : this.formParams.entrySet()) {
            params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        this.request.setEntity(new UrlEncodedFormEntity(params, UTF_8));
        return this;
    }

    /**
     * 设置普通表单参数（批量）
     *
     * @param formParams 普通表单参数
     * @return HttpUtil
     */
    public HttpUtil setFormParams(Map<String, String> formParams) {
        if (formParams != null) {
            this.formParams = formParams;
            List<NameValuePair> params = new ArrayList<>();
            for (Map.Entry<String, String> entry : this.formParams.entrySet()) {
                params.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            this.request.setEntity(new UrlEncodedFormEntity(params, UTF_8));
        }
        return this;
    }

    /**
     * 设置上传表单参数（单个）
     *
     * @param key   上传表单参数名称
     * @param value 上传表单参数值
     * @return HttpUtil
     */
    public HttpUtil setUploadFormParam(String key, Object value) {
        if (this.uploadFormParams == null) {
            this.uploadFormParams = new LinkedHashMap<>();
        }
        if (value instanceof String) {
            this.uploadFormParams.put(key, value);
        } else if (value instanceof File) {
            this.uploadFormParams.put(key, value);
        } else {
            logger.error("参数类型错误，仅支持String和File类型。");
        }
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setContentType(ContentType.MULTIPART_FORM_DATA);
        for (Map.Entry<String, Object> uploadFormParam : this.uploadFormParams.entrySet()) {
            String tmpKey = uploadFormParam.getKey();
            Object tmpValue = uploadFormParam.getValue();
            if (tmpValue instanceof String) {
                multipartEntityBuilder.addTextBody(tmpKey, (String) tmpValue);
            } else if (tmpValue instanceof File) {
                File file = (File) tmpValue;
                String filename = file.getName();
                ContentType contentType = this.getFileContentType(filename);
                multipartEntityBuilder.addBinaryBody(tmpKey, file, contentType, filename);
            } else {
                logger.error("参数类型错误，仅支持String和File类型。");
            }
        }
        this.request.setEntity(multipartEntityBuilder.build());
        return this;
    }

    /**
     * 设置上传表单参数（批量）
     *
     * @param uploadFormParams 上传表单参数
     * @return HttpUtil
     */
    public HttpUtil setUploadFormParams(Map<String, Object> uploadFormParams) {
        MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create().setContentType(ContentType.MULTIPART_FORM_DATA);
        if (uploadFormParams != null) {
            this.uploadFormParams = new HashMap<>();
            for (Map.Entry<String, Object> uploadFormParam : uploadFormParams.entrySet()) {
                String key = uploadFormParam.getKey();
                Object value = uploadFormParam.getValue();
                if (value instanceof String) {
                    multipartEntityBuilder.addTextBody(key, (String) value);
                    this.uploadFormParams.put(key, value);
                } else if (value instanceof File) {
                    File file = (File) value;
                    String filename = file.getName();
                    ContentType contentType = this.getFileContentType(filename);
                    multipartEntityBuilder.addBinaryBody(key, file, contentType, filename);
                    this.uploadFormParams.put(key, value);
                } else {
                    logger.error("参数类型错误，仅支持String和File类型。");
                }
            }
            this.request.setEntity(multipartEntityBuilder.build());
        }
        return this;
    }

    /**
     * 获取文件Content-Type
     *
     * @param filename 文件名
     * @return Content-Type
     */
    private ContentType getFileContentType(String filename) {
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        switch (suffix) {
            case "jpg":
            case "jpeg":
                return ContentType.IMAGE_JPEG;
            case "png":
                return ContentType.IMAGE_PNG;
            case "bmp":
                return ContentType.IMAGE_BMP;
            case "gif":
                return ContentType.IMAGE_GIF;
            case "svg":
                return ContentType.IMAGE_SVG;
            default:
                logger.error("文件格式 {} 不受支持。", suffix);
                return null;
        }
    }

    /**
     * 设置JSON参数
     *
     * @param jsonParams JSON参数
     * @return HttpUtil
     */
    public HttpUtil setJsonParams(String jsonParams) {
        if (jsonParams != null) {
            this.jsonParams = jsonParams;
            this.request.setEntity(new StringEntity(jsonParams, ContentType.APPLICATION_JSON));
        }
        return this;
    }

    /**
     * 设置请求头（单个）
     *
     * @param key   请求头名称
     * @param value 请求头值
     * @return HttpUtil
     */
    public HttpUtil setHeader(String key, String value) {
        if (this.headers == null) {
            this.headers = new HashMap<>();
        }
        this.headers.put(key, value);
        this.request.setHeader(key, value);
        return this;
    }

    /**
     * 设置请求头（批量）
     *
     * @param headers 请求头
     * @return HttpUtil
     */
    public HttpUtil setHeaders(Map<String, String> headers) {
        if (headers != null) {
            this.headers = headers;
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                this.request.setHeader(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    /**
     * 执行HTTP请求
     *
     * @return HttpUtil
     * @throws IOException IO异常
     */
    public HttpUtil execute() throws IOException {
        this.response = this.client.execute(this.request);
        this.queryParams = null;
        this.formParams = null;
        this.uploadFormParams = null;
        this.jsonParams = "";
        return this;
    }

    /**
     * 以字符串方式获取响应体
     *
     * @return 响应体
     */
    public String getResponseAsString() {
        HttpEntity entity = this.response.getEntity();
        if (entity == null) {
            return "";
        } else {
            try {
                InputStream stream = entity.getContent();
                if (stream == null) {
                    return "";
                } else {
                    return IOUtils.toString(stream, StandardCharsets.UTF_8);
                }
            } catch (IOException e) {
                logger.error("获取HTTP请求 {} {} 的响应体失败。", this.method, this.url);
                return "";
            }
        }
    }

    /**
     * 以输入流方式获取响应体
     *
     * @return 响应体
     */
    public InputStream getResponseAsStream() {
        HttpEntity entity = this.response.getEntity();
        if (entity == null) {
            return null;
        } else {
            try {
                return entity.getContent();
            } catch (IOException e) {
                logger.error("获取HTTP请求 {} {} 的响应体失败。", this.method, this.url);
                return null;
            }
        }
    }

    /**
     * 获取响应头
     *
     * @return 响应头
     */
    public Map<String, String> getResponseHeaders() {
        Header[] headers = this.response.getHeaders();
        Map<String, String> result = new HashMap<>();
        for (Header header : headers) {
            result.put(header.getName(), header.getValue());
        }
        return result;
    }

    /**
     * 获取HTTP客户端
     *
     * @return HTTP客户端
     */
    public CloseableHttpClient getClient() {
        return this.client;
    }

    /**
     * 获取请求对象
     *
     * @return 请求对象
     */
    public HttpUriRequestBase getRequest() {
        return this.request;
    }

    /**
     * 获取响应对象
     *
     * @return 响应对象
     */
    public CloseableHttpResponse getResponse() {
        return this.response;
    }

    @Override
    public void close() throws IOException {
        if (this.response != null) {
            this.response.close();
        }
        if (this.client != null) {
            this.client.close();
        }
    }

}

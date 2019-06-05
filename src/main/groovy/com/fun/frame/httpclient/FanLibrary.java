package com.fun.frame.httpclient;

import com.fun.base.bean.RequestInfo;
import com.fun.base.interfaces.IBase;
import com.fun.config.HttpClientConstant;
import com.fun.config.SysInit;
import com.fun.frame.SourceCode;
import com.fun.utils.DecodeEncode;
import com.fun.utils.Time;
import com.fun.utils.db.mysql.MySqlTest;
import com.fun.utils.message.AlertOver;
import net.sf.json.JSONObject;
import org.apache.http.*;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 请求相关类，采用统一的静态方法，在登录后台管理页面是自动化设置cookie，其他公参由各自的base类实现header
 */
public class FanLibrary extends SourceCode {

    private static Logger logger = LoggerFactory.getLogger(FanLibrary.class);

    private static IBase iBase;

    /**
     * 请求超时控制器
     */
    public static RequestConfig requestConfig = getRequestConfig();

    /**
     * 重置响应超时时间
     *
     * @param timeOut 单位s
     */
    public static void setSocketTimeOut(int timeOut) {
        HttpClientConstant.SOCKET_TIMEOUT = timeOut * 1000;
        requestConfig = getRequestConfig();
    }

    /**
     * 打印请求头和响应头，一次有效，在请求之前使用该方法
     */
    public static void setHeaderKey() {
        HEADER_KEY = true;
    }

    /**
     * 最近发送的请求
     */
    public static LinkedList<HttpRequestBase> requests = new LinkedList<>();

    /**
     * 是否显示请求所有header的开关
     */
    static boolean HEADER_KEY = false;

    /**
     * 方法已重载，获取get对象
     * <p>方法重载，主要区别参数，会自动进行urlencode操作</p>
     *
     * @param url  表示请求地址
     * @param args 表示传入数据
     * @return 返回get对象
     */
    public static HttpGet getHttpGet(String url, JSONObject args) {
        if (args == null || args.isEmpty())
            return getHttpGet(url);
        String uri = url + changeJsonToArguments(args);
        return getHttpGet(uri.replace(" ", ""));
    }

    /**
     * 方法已重载，获取get对象
     * <p>方法重载，主要区别参数，会自动进行urlencode操作</p>
     *
     * @param url 表示请求地址
     * @return 返回get对象
     */
    public static HttpGet getHttpGet(String url) {
        return new HttpGet(url);
    }

    /**
     * 获取post对象，以form表单提交数据
     * <p>方法重载，文字信息form表单提交，文件信息二进制流提交，具体参照文件上传的方法主食，post请求可以不需要参数，暂时不支持其他参数类型，如果是公参需要在url里面展示，需要传一个json对象，一般默认args为get公参，params为post请求参数</p>
     *
     * @param url    请求地址
     * @param params 请求数据，form表单形式设置请求实体
     * @return 返回post对象
     */
    public static HttpPost getHttpPost(String url, JSONObject params) {
        HttpPost httpPost = getHttpPost(url);
        setFormHttpEntity(httpPost, params);
        httpPost.addHeader(HttpClientConstant.ContentType_FORM);
        return httpPost;
    }

    /**
     * 获取httppost对象，没有参数设置
     * <p>方法重载，文字信息form表单提交，文件信息二进制流提交，具体参照文件上传的方法主食，post请求可以不需要参数，暂时不支持其他参数类型，如果是公参需要在url里面展示，需要传一个json对象，一般默认args为get公参，params为post请求参数</p>
     *
     * @param url
     * @return
     */
    public static HttpPost getHttpPost(String url) {
        return new HttpPost(url);
    }

    /**
     * 获取httppost对象，json格式对象，传参时手动tostring
     * <p>新重载方法，适应post请求json传参，估计utf-8编码格式</p>
     *
     * @param url
     * @param params
     * @return
     */
    public static HttpPost getHttpPost(String url, String params) {
        HttpPost httpPost = getHttpPost(url);
        httpPost.setEntity(new StringEntity(params, DEFAULT_CHARSET.toString()));
        httpPost.addHeader(HttpClientConstant.ContentType_JSON);
        return httpPost;
    }

    /**
     * * 获取httppost对象，json格式对象，传参时手动tostring
     * <p>新重载方法，适应post请求json传参</p>
     *
     * @param url
     * @param args
     * @return
     */
    public static HttpPost getHttpPost(String url, JSONObject args, String params) {
        return getHttpPost(url + changeJsonToArguments(args), params);
    }

    /**
     * 获取 httppost 请求对象
     * <p>方法重载，文字信息form表单提交，文件信息二进制流提交，具体参照文件上传的方法主食，post请求可以不需要参数，暂时不支持其他参数类型，如果是公参需要在url里面展示，需要传一个json对象，一般默认args为get公参，params为post请求参数</p>
     *
     * @param url    请求地址
     * @param args   请求地址参数
     * @param params 请求参数
     * @return
     */
    public static HttpPost getHttpPost(String url, JSONObject args, JSONObject params) {
        return getHttpPost(url + changeJsonToArguments(args), params);
    }


    /**
     * 获取 httpPost 对象
     * <p>方法重载，文字信息form表单提交，文件信息二进制流提交，具体参照文件上传的方法主食，post请求可以不需要参数，暂时不支持其他参数类型，如果是公参需要在url里面展示，需要传一个json对象，一般默认args为get公参，params为post请求参数</p>
     *
     * @param url    请求地址
     * @param args   请求通用参数
     * @param params 请求参数，其中二进制流必须是 file
     * @param file   文件
     * @return
     */
    public static HttpPost getHttpPost(String url, JSONObject args, JSONObject params, File file) {
        return getHttpPost(url + changeJsonToArguments(args), params, file);
    }

    /**
     * 获取 httpPost 对象
     * <p>方法重载，文字信息form表单提交，文件信息二进制流提交，具体参照文件上传的方法主食，post请求可以不需要参数，暂时不支持其他参数类型，如果是公参需要在url里面展示，需要传一个json对象，一般默认args为get公参，params为post请求参数</p>
     *
     * @param url    请求地址
     * @param params 请求参数，其中二进制流必须是 file
     * @param file   文件
     * @return
     */
    public static HttpPost getHttpPost(String url, JSONObject params, File file) {
        HttpPost httpPost = getHttpPost(url);
        setMultipartEntityEntity(httpPost, params, file);
        return httpPost;
    }

    /**
     * 设置二进制流实体，params 里面参数值为 file
     *
     * @param httpPost httpPsot 请求
     * @param params   请求参数
     * @param file     文件
     */
    private static void setMultipartEntityEntity(HttpPost httpPost, JSONObject params, File file) {
        String fileName = file.getName();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            logger.warn("读取文件失败！", e);
        }
        Iterator<String> keys = params.keys();// 遍历 params 参数和值
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();// 新建MultipartEntityBuilder对象
        while (keys.hasNext()) {
            String key = keys.next();
            String value = params.getString(key);
            if (value.equals("file")) {
                builder.addBinaryBody(key, inputStream, ContentType.create(HttpClientConstant.CONTENTTYPE_MULTIPART_FORM), fileName);// 设置流参数
            } else {
                StringBody body = new StringBody(value, ContentType.create(HttpClientConstant.CONTENTTYPE_TEXT, DEFAULT_CHARSET));// 设置普通参数
                builder.addPart(key, body);
            }
        }
        HttpEntity entity = builder.build();
        httpPost.setEntity(entity);
    }

    /**
     * 获取请求超时控制器
     *
     * @return
     */
    private static RequestConfig getRequestConfig() {
        return RequestConfig.custom().setConnectionRequestTimeout(HttpClientConstant.CONNECT_REQUEST_TIMEOUT).setConnectTimeout(HttpClientConstant.CONNECT_TIMEOUT).setSocketTimeout(HttpClientConstant.SOCKET_TIMEOUT).setCookieSpec(CookieSpecs.STANDARD).build();
    }

    /**
     * 发送请求之前，配置请求管理器，设置IP，user_agent和cookie
     *
     * @param request
     */
    protected static void beforeRequest(HttpRequestBase request) {
        request.setConfig(requestConfig);//设置请求配置
        HttpClientConstant.COMMON_HEADER.forEach(header -> request.addHeader(header));
    }

    /**
     * 响应结束之后，处理响应头信息，如set-cookien内容
     *
     * @param response 响应内容
     * @return
     */
    private static JSONObject afterResponse(CloseableHttpResponse response) {
        JSONObject cookies = new JSONObject();
        List<Header> headers = Arrays.asList(response.getHeaders("Set-Cookie"));
        if (headers.size() == 0) return cookies;
        headers.forEach(x -> {
            String[] split = x.getValue().split(";")[0].split("=", 2);
            cookies.put(split[0], split[1]);
        });
        return cookies;
    }

    /**
     * 根据解析好的content，转化json对象
     *
     * @param content
     * @return
     */
    private static JSONObject getJsonResponse(String content, JSONObject cookies) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = JSONObject.fromObject(content);
        } catch (Exception e) {
            jsonObject.put("content", content);
            jsonObject.put("code", TEST_ERROR_CODE);
            logger.warn("响应体非json格式，已经自动转换成json格式！");
        } finally {
            if (!cookies.isEmpty()) jsonObject.put(HttpClientConstant.COOKIE, cookies);
            return jsonObject;
        }
    }

    /**
     * 根据响应获取响应实体
     *
     * @param response
     * @return
     */
    public static String getContent(CloseableHttpResponse response) {
        HttpEntity entity = response.getEntity();// 获取响应实体
        String content = EMPTY;
        try {
            content = EntityUtils.toString(entity, DEFAULT_CHARSET);// 用string接收响应实体
            EntityUtils.consume(entity);// 消耗响应实体，并关闭相关资源占用
        } catch (Exception e1) {
            logger.warn("解析响应实体异常！", e1);
        }
        return content;
    }

    /**
     * 获取响应实体
     *
     * @param request 请求对象
     * @return 返回json类型的对象
     */
    public static JSONObject getHttpResponse(HttpRequestBase request) {
        if (!request.getURI().toString().toLowerCase().startsWith("http")) return null;
        beforeRequest(request);
        JSONObject jsonObject = new JSONObject();
        RequestInfo requestInfo = new RequestInfo(request);
        if (HEADER_KEY) output(request.getAllHeaders());
        long start = Time.getTimeStamp();
        try (CloseableHttpResponse response = ClientManage.httpsClient.execute(request)) {
            long end = Time.getTimeStamp();
            long elapsed_time = end - start;
            if (HEADER_KEY) output(response.getAllHeaders());
            if (response == null) return jsonObject;
            int status = response.getStatusLine().getStatusCode();
            JSONObject setCookies = afterResponse(response);
            String content = getContent(response);
            int data_size = content.length();
            jsonObject = getJsonResponse(content, setCookies);
            int code = iBase == null ? -2 : iBase.checkCode(jsonObject, requestInfo);
            if (status != HttpStatus.SC_OK)
                new AlertOver("响应状态码错误", "状态码错误：" + status, requestInfo.getUrl(), requestInfo).sendSystemMessage();
            MySqlTest.saveApiTestDate(requestInfo, data_size, elapsed_time, status, getMark(), code, LOCAL_IP, COMPUTER_USER_NAME);
        } catch (Exception e) {
            logger.warn("获取请求相应失败！", e);
            if (!SysInit.isBlack(requestInfo.getHost()))
                new AlertOver("接口请求失败", requestInfo.toString(), requestInfo.getUrl(), requestInfo).sendSystemMessage();
        } finally {
            HEADER_KEY = false;
            if (!SysInit.isBlack(requestInfo.getHost())) {
                if (requests.size() > 9) requests.removeFirst();
                boolean add = requests.add(request);
            }
        }
        return jsonObject;
    }

    /**
     * 设置post接口上传表单，默认的编码格式
     *
     * @param httpPost post请求
     * @param params   参数
     */
    private static void setFormHttpEntity(HttpPost httpPost, JSONObject params) {
        List<NameValuePair> formparams = new ArrayList<NameValuePair>();
        params.keySet().forEach(x -> formparams.add(new BasicNameValuePair(x.toString(), params.getString(x.toString()))));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, DEFAULT_CHARSET);
        httpPost.setEntity(entity);
    }

    /**
     * 解析response，使用char数组，注意编码格式
     * <p>自定义解析响应实体的方法，暂不采用</p>
     *
     * @param response 传入的response，非closedresponse
     * @return string类型的response
     */
    @Deprecated
    private static String parseResponeEntityByChar(HttpResponse response) {
        StringBuffer buffer = new StringBuffer();// 创建并实例化stringbuffer，存放响应信息
        try (InputStream input = response.getEntity().getContent(); InputStreamReader reader = new InputStreamReader(input, DEFAULT_CHARSET)) {
            char[] buff = new char[1024];// 创建并实例化字符数组
            int length = 0;// 声明变量length，表示读取长度
            while ((length = reader.read(buff)) != -1) {// 循环读取字符输入流
                String x = new String(buff, 0, length);// 获取读取到的有效内容
                buffer.append(x);// 将读取到的内容添加到stringbuffer中
            }
        } catch (IOException e) {
            logger.warn("解析响应实体失败！", e);
        }
        return buffer.toString();
    }

    /**
     * 把json数据转化为参数，为get请求和post请求stringentity的时候使用
     *
     * @param argument 请求参数，json数据类型，map类型，可转化
     * @return 返回拼接参数后的地址
     */
    public static String changeJsonToArguments(JSONObject argument) {
        return argument == null || argument.isEmpty() ? EMPTY : argument.keySet().stream().map(x -> x.toString() + "=" + DecodeEncode.urlEncoderText(argument.getString(x.toString()))).collect(Collectors.joining("&", "?", "")).toString();
    }

    /**
     * 通过json对象信息，生成cookie的header
     *
     * @param cookies
     * @return
     */
    public static Header getCookies(JSONObject cookies) {
        return getHeader(HttpClientConstant.COOKIE, cookies.keySet().stream().map(x -> x.toString() + "=" + cookies.get(x).toString()).collect(Collectors.joining(";")).toString());
    }

    /**
     * 生成header
     *
     * @param name
     * @param value
     * @return
     */
    public static Header getHeader(String name, String value) {
        return new BasicHeader(name, value);
    }

    public static IBase getiBase() {
        return iBase;
    }

    public static void setiBase(IBase iBase) {
        FanLibrary.iBase = iBase;
    }

    /**
     * 获取最后一个发出的请求
     *
     * @return
     */
    public static HttpRequestBase getLastRequest() {
        return requests.getLast();
    }

    /**
     * 结束测试，关闭连接池
     */
    public static void testOver() {
        MySqlTest.mySqlQueryOver();
        try {
            ClientManage.httpsClient.close();
        } catch (IOException e) {
            logger.warn("连接池关闭失败！", e);
        }
    }

}
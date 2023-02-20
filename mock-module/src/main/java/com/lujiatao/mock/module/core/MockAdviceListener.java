package com.lujiatao.mock.module.core;

import com.alibaba.fastjson2.JSON;
import com.alibaba.jvm.sandbox.api.ProcessControlException;
import com.alibaba.jvm.sandbox.api.ProcessController;
import com.alibaba.jvm.sandbox.api.listener.ext.Advice;
import com.alibaba.jvm.sandbox.api.listener.ext.AdviceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock通知监听器
 *
 * @author 卢家涛
 */
public class MockAdviceListener extends AdviceListener {

    private static final Logger logger = LoggerFactory.getLogger(MockAdviceListener.class);

    private final MockConfig mockConfig;

    public MockAdviceListener(MockConfig mockConfig) {
        this.mockConfig = mockConfig;
    }

    /**
     * 方法返回/抛出异常前调用
     *
     * @param advice 通知
     * @throws Throwable 处理通知报错
     */
    protected void after(Advice advice) throws Throwable {
        try {
            ParameterRule[] parameterRules = mockConfig.getParameterRules();
            String returnOrThrowData = mockConfig.getReturnOrThrowData();
            Object[] parameters = advice.getParameterArray();
            int expected = parameterRules.length;
            int actual = parameters.length;
            // 未配置参数时，不进行参数匹配。
            if (expected == 0) {
                returnOrThrow(returnOrThrowData, advice);
            }
            // 已配置参数且参数数量一致时，进行参数匹配。
            else if (expected == actual) {
                for (int i = 0; i < parameterRules.length; i++) {
                    if (!isMatchParameter(parameterRules[i], parameters[i])) {
                        return;
                    }
                }
                returnOrThrow(returnOrThrowData, advice);
            }
            // 已配置参数且参数数量不一致时，抛出异常。
            else {
                throw new RuntimeException("实际参数数量(" + actual + "个)与期望参数数量(" + expected + "个)不一致。");
            }
        } catch (ProcessControlException e) {
            // ProcessControlException不能阻断，否则Mock不生效。
            throw e;
        } catch (Exception e) {
            logger.error("Mock失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 匹配参数
     *
     * @param parameterRule 期望参数规则
     * @param parameter     实际参数
     * @return 匹配结果
     */
    private boolean isMatchParameter(ParameterRule parameterRule, Object parameter) {
        String patternName = parameterRule.getPattern().name();
        if (patternName.equals("IGNORE")) {
            return true;
        }
        String parameterType = parameter.getClass().getName();
        String type = parameterRule.getType();
        if (!parameterType.equals(type)) {
            return false;
        }
        switch (patternName) {
            case "CONTAIN": {
                if (parameterType.equals("java.lang.String")) {
                    return ((String) parameter).contains(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            case "NOT_CONTAIN": {
                if (parameterType.equals("java.lang.String")) {
                    return !((String) parameter).contains(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            case "EQUAL": {
                switch (parameterType) {
                    case "java.lang.String":
                        return parameter.equals(parameterRule.getValue());
                    case "java.lang.Integer":
                        return (Integer) parameter == Integer.parseInt(parameterRule.getValue());
                    case "java.lang.Double":
                        return (Double) parameter == Double.parseDouble(parameterRule.getValue());
                    case "java.lang.Boolean":
                        return (Boolean) parameter == Boolean.parseBoolean(parameterRule.getValue());
                    default:
                        return false;
                }
            }
            case "NOT_EQUAL": {
                switch (parameterType) {
                    case "java.lang.String":
                        return !parameter.equals(parameterRule.getValue());
                    case "java.lang.Integer":
                        return (Integer) parameter != Integer.parseInt(parameterRule.getValue());
                    case "java.lang.Double":
                        return (Double) parameter != Double.parseDouble(parameterRule.getValue());
                    case "java.lang.Boolean":
                        return (Boolean) parameter != Boolean.parseBoolean(parameterRule.getValue());
                    default:
                        return false;
                }
            }
            case "MORE_THAN": {
                if (parameterType.equals("java.lang.Integer")) {
                    return (Integer) parameter > Integer.parseInt(parameterRule.getValue());
                } else if (parameterType.equals("java.lang.Double")) {
                    return (Double) parameter > Double.parseDouble(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            case "MORE_THAN_OR_EQUAL": {
                if (parameterType.equals("java.lang.Integer")) {
                    return (Integer) parameter >= Integer.parseInt(parameterRule.getValue());
                } else if (parameterType.equals("java.lang.Double")) {
                    return (Double) parameter >= Double.parseDouble(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            case "LESS_THAN": {
                if (parameterType.equals("java.lang.Integer")) {
                    return (Integer) parameter < Integer.parseInt(parameterRule.getValue());
                } else if (parameterType.equals("java.lang.Double")) {
                    return (Double) parameter < Double.parseDouble(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            case "LESS_THAN_OR_EQUAL": {
                if (parameterType.equals("java.lang.Integer")) {
                    return (Integer) parameter <= Integer.parseInt(parameterRule.getValue());
                } else if (parameterType.equals("java.lang.Double")) {
                    return (Double) parameter <= Double.parseDouble(parameterRule.getValue());
                } else {
                    return false;
                }
            }
            default:
                return false;
        }
    }

    /**
     * 返回或抛出
     *
     * @param returnOrThrowData 待返回或抛出的数据
     * @param advice            通知
     * @throws Exception 返回或抛出报错
     */
    private void returnOrThrow(String returnOrThrowData, Advice advice) throws Exception {
        /*
        1. 返回JSON列表
        2. 返回JSON
         */
        if (returnOrThrowData.startsWith("[{") && returnOrThrowData.endsWith("}]") ||
                returnOrThrowData.startsWith("{") && returnOrThrowData.endsWith("}")) {
            Object object = JSON.parseObject(returnOrThrowData, advice.getReturnObj().getClass());
            ProcessController.returnImmediately(object);
        } else {
            // 3. 抛出异常
            Class<?> exceptionClass = null;
            boolean isException;
            try {
                exceptionClass = this.getClass().getClassLoader().loadClass(returnOrThrowData);
                isException = Exception.class.isAssignableFrom(exceptionClass);
            } catch (ClassNotFoundException e) {
                isException = false;
            }
            if (isException) {
                ProcessController.throwsImmediately(((Class<Exception>) exceptionClass).getDeclaredConstructor().newInstance());
            }
            /*
            4. 返回null
            5. 返回String
            6. 返回基本类型
             */
            else {
                Object object = this.stringToObject(returnOrThrowData, advice.getReturnObj().getClass());
                ProcessController.returnImmediately(object);
            }
        }
    }

    /**
     * 字符串转换成对象
     *
     * @param string 字符串
     * @param aClass 目标对象
     * @return 对象
     */
    private Object stringToObject(String string, Class<?> aClass) {
        if (string.equals("null")) {
            return null;
        } else if (String.class.equals(aClass)) {
            return string;
        } else if (byte.class.equals(aClass) || Byte.class.equals(aClass)) {
            return Byte.valueOf(string);
        } else if (short.class.equals(aClass) || Short.class.equals(aClass)) {
            return Short.valueOf(string);
        } else if (int.class.equals(aClass) || Integer.class.equals(aClass)) {
            return Integer.valueOf(string);
        } else if (long.class.equals(aClass) || Long.class.equals(aClass)) {
            return Long.valueOf(string);
        } else if (float.class.equals(aClass) || Float.class.equals(aClass)) {
            return Float.valueOf(string);
        } else if (double.class.equals(aClass) || Double.class.equals(aClass)) {
            return Double.valueOf(string);
        } else if (boolean.class.equals(aClass) || Boolean.class.equals(aClass)) {
            return Boolean.valueOf(string);
        } else if ((char.class.equals(aClass) || Character.class.equals(aClass)) && string.length() == 1) {
            return string.charAt(0);
        } else {
            throw new RuntimeException("字符串" + string + "无法转换成目标对象。");
        }
    }

}

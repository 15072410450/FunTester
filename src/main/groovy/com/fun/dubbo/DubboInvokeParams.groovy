package com.fun.dubbo

class DubboInvokeParams {

    int length

    String[] types = new String[length]

    Object[] values = new Objects[length]

    DubboInvokeParams(int length) {
        this.length = length;
    }

}

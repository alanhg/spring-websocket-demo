package com.example.messagingstompwebsocket;

import java.security.Principal;
import java.util.function.Function;

public class MyPrincipal implements Principal {
    private String uid;
    private String name;
    /**
     * 例子:alan@1991421.cn‖Alan He
     */
    public static Function<MyPrincipal, String> userKeyFn = (principal) -> principal.getUid() + "‖" + principal.getName();

    public MyPrincipal(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getUid() {
        return uid;
    }
}

package com.sim.spriced.application.productcatalogue;



import com.sim.spriced.platform.BusinessInterface.ApplicationInterface;
import com.sim.spriced.platform.BusinessInterface.PlatformContext;
import com.sim.spriced.platform.BusinessInterface.TransactionData;
import com.sim.spriced.platform.commons_management_layer.Enums.ValidationStatus;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;

@Service("TestFunctionImpl/1.0")
public class TestFunctionImpl_1_0 implements ApplicationInterface {

    public TestFunctionImpl_1_0() {
    }


    @Override
    public TransactionData execute(PlatformContext context, TransactionData data) {
        return null;
    }

    @Override
    public ValidationStatus validate(TransactionData message) {
        return null;
    }

    @Override
    public TransactionData transform(TransactionData requestData) {
        return null;
    }
}
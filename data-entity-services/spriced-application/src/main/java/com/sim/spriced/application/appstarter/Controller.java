package com.sim.spriced.application.appstarter;

import com.sim.spriced.application.productcatalogue.TestFunctionImpl_1_0;
import com.sim.spriced.platform.commons_management_layer.Models.Transaction;
import com.sim.spriced.platform.BusinessInterface.handler.FunctionDispatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/spriced/application/dataentity")
public class Controller {


    private final FunctionDispatcher functionDispatcher;

    @Autowired
    TestFunctionImpl_1_0 testFunctionImpl10;


    @Autowired
    public Controller(FunctionDispatcher functionDispatcher) {
        this.functionDispatcher = functionDispatcher;
    }

    @PostMapping("")
    public ResponseEntity<Transaction> process(@RequestBody Transaction request)
    {
        Transaction response = this.functionDispatcher.dispatch(request);
        try {
            System.out.println("Response -- "+request);
            System.currentTimeMillis();
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

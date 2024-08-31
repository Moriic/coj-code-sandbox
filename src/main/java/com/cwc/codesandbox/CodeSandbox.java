package com.cwc.codesandbox;


import com.cwc.model.ExecuteCodeRequest;
import com.cwc.model.ExecuteCodeResponse;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}

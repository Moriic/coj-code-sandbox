package com.cwc.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExecuteCodeResponse {
    private List<String> outputList;

    private String message;

    private Integer status;

    private JudgeInfo judgeInfo;
}

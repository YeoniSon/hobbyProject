package com.example.interaction.dto.request;

import com.example.common.enums.TargetType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportRequest {
    private TargetType targetType;
    private String reason;
}

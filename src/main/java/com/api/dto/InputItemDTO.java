package com.api.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.api.pojo.InputItemDTODeserializer;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
@JsonDeserialize(using = InputItemDTODeserializer.class)
public final class InputItemDTO {

    private final double currentPrice;
    private final String description;
    private final int sequence;
    private final String barcode;
}

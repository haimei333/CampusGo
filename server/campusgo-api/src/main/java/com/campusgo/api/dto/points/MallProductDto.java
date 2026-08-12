package com.campusgo.api.dto.points;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Schema(description = "积分商城商品")
public class MallProductDto {

    String id;
    String name;
    String subtitle;
    String category;
    int pointsCost;
    int stock;
    String emoji;
    boolean flashSale;
}
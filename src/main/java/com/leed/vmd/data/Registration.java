package com.leed.vmd.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Registration
{
    final String name;
    final String businessName;
    final String email;
    final String mobile;
}

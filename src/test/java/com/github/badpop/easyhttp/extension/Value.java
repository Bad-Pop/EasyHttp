package com.github.badpop.easyhttp.extension;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@lombok.Value
@AllArgsConstructor
public class Value {
  @JsonProperty(value = "value")
  int value;
}

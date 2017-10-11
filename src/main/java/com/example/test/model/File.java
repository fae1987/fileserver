package com.example.test.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
@Getter
public class File {

    String id;

    String name;
}

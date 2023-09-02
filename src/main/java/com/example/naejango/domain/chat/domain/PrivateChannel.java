package com.example.naejango.domain.chat.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("PRIVATE")
@Getter
@NoArgsConstructor
@ToString
@SuperBuilder
public class PrivateChannel extends Channel {
}

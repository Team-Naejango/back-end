package com.example.naejango.domain.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Channel {
    @Id @GeneratedValue
    @Column(name = "channel_id")
    private Long id;

    @OneToMany(mappedBy = "channel")
    private List<ChannelUser> channelUsers;
}


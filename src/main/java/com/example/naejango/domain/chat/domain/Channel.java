package com.example.naejango.domain.chat.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;

@Entity
@Table(name = "channel")
/* SINGLE_TABLE 을 전략으로 하위 Channel 을 구분합니다.(PRIVATE / GROUP)
* 해당 전략은 하나의 테이블에 모든 자식 엔티티를 맵핑 하는 전략입니다.
* 단순한 구조이기 때문에 다루기 용이하고 조인할 필요가 없어 빠른 조회 속도를 가지지만
* 정규화를 하지 않으므로 테이블 크기가 증가하고 데이터의 낭비가 생길 수 있습니다.
* 하지만 Channel 의 경우 계층구조가 단순하고 큰 확장성이 요구 되지 않는 상황이므로
* SINGLE_TABLE 을 선택하였습니다.
*/
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "channel_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Channel {
    @Id @GeneratedValue
    @Column(name = "channel_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(insertable = false, updatable = false)
    private ChannelType channelType;
    private Long lastMessageId;
}


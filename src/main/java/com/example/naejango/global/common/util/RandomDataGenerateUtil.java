package com.example.naejango.global.common.util;

import com.example.naejango.domain.item.domain.ItemType;
import com.example.naejango.domain.user.domain.Gender;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@Component
@RequiredArgsConstructor
public class RandomDataGenerateUtil {

    private final Random random = new Random();
    private final GeomUtil geomUtil;
    private final String[] nicknameAdj = {
            "밝은", "아름다운", "창조적인", "대담한", "빠른", "똑똑한", "용감한", "기발한", "날렵한", "웅장한", "차분한", "섬세한",
            "매력적인", "활발한", "강인한", "화려한", "감각적인", "진실한", "경쾌한", "유려한", "무한한", "미스테리한", "귀여운", "낭만적인",
            "열정적인", "화끈한", "빛나는", "고요한", "자유로운", "소박한", "과감한", "편안한", "따뜻한", "시원한", "기운찬", "유쾌한",
            "명랑한", "차가운", "뜨거운", "순수한", "깔끔한", "황홀한", "잔잔한", "사랑스러운", "신비로운", "애정적인", "자연스러운", "창의적인", "풍부한", "싱그러운"
    };

    private final String[] animalName = {
            "호랑이", "사자", "기린", "펭귄", "코끼리", "캥거루", "팬더", "곰", "토끼", "늑대", "여우", "독수리", "뱀", "원숭이",
            "사슴", "고래", "상어", "펠리컨", "프레리독", "코뿔소", "표범", "청소기", "두루미", "햄스터", "나비", "앵무새", "거북이",
            "비둘기", "하이에나", "가오리"
    };

    private final String[] storageAdj = {
            "현대", "전통", "아기자기", "유니크", "창조", "프리미엄", "고품질", "핸드메이드", "친환경", "아트", "전문가", "컬러풀", "미니멀",
            "부드러움", "리트로", "모던", "럭셔리", "친절한", "정겨움", "개성", "저렴", "고급", "편함", "빈티지", "트렌드", "알뜰", "대박"
    };

    private final String[] storageName = {
            "샵", "가게", "장터", "배달", "창고", "창고", "샵", "발견", "마켓", "마켓", "선택", "명품", "싸다싸", "가게",
            "집", "집", "", "팝니다", "삽니다", "삼", "팜", "패션", "나눔", "나눔", "", "빈티지", "트렌드", "알뜰", "장사꾼"
    };

    private final String[] itemName = {
            "치약", "휴지", "물티슈", "선풍기", "생리대", "세면도구", "이불", "마스크", "샴푸", "린스", "헤어드라이어",
            "핸드워시", "욕실용품", "세제", "키친타올", "휴대폰충전기", "면봉", "스티커", "수건", "기저귀", "세탁세제",
            "종이컵", "손소독제", "칫솔", "양말", "화장지"
    };

    public String getRandomItemName() {
        return getRandomElement(itemName);
    }

    public String getItemDescription(String itemName, ItemType type) {
        if (type.equals(ItemType.INDIVIDUAL_BUY)) {
            return itemName + "필요해요.";
        }
        return itemName + "팔아요.";
    }

    public ItemType getRandomItemType() {
        if (random.nextBoolean()) return ItemType.INDIVIDUAL_BUY;
        return ItemType.INDIVIDUAL_SELL;
    }

    public boolean getRandomBoolean() {
        return random.nextBoolean();
    }

    public int getRandomInt(int n) {
        return random.nextInt(n);
    }

    public String getRandomNickname() {
        return getRandomElement(nicknameAdj) + getRandomElement(animalName);
    }

    public String getRandomStorageName() {
        return getRandomElement(storageAdj) + getRandomElement(storageName);
    }

    public String getRandomImageUrl() {
        String imageUrl = "https://picsum.photos/250/250";
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.getForEntity(imageUrl, Void.class);
        HttpHeaders headers = response.getHeaders();
        return headers.getFirst(HttpHeaders.LOCATION);
    }

    public Point getRandomPointInGangnam() {
        final double MIN_LATITUDE = 37.473824;
        final double MAX_LATITUDE = 37.517071;
        final double MIN_LONGITUDE = 127.014418;
        final double MAX_LONGITUDE = 127.060426;

        double randomLatitude = MIN_LATITUDE + (MAX_LATITUDE - MIN_LATITUDE) * random.nextDouble();
        double randomLongitude = MIN_LONGITUDE + (MAX_LONGITUDE - MIN_LONGITUDE) * random.nextDouble();
        return geomUtil.createPoint(randomLongitude, randomLatitude);
    }

    public Gender getRandomGender() {
        if (random.nextBoolean()) return Gender.MALE;
        return Gender.FEMALE;
    }

    public String getRandomBirth() {
        int ratio = random.nextInt(100);
        int year = 2023;
        int birthYear;

        if (ratio < 65) {
            // 20 ~ 39 세 비율 65%
            birthYear = year - (20 + random.nextInt(20));
        } else if (ratio < 90) {
            // 10대 40대 비율 25%
            if (random.nextBoolean()) {
                birthYear = year - (10 + random.nextInt(10)); // 10 ~ 19 years old
            } else {
                birthYear = year - (40 + random.nextInt(10)); // 40 ~ 49 years old
            }
        } else {
            // 60, 70대 비율 10%
            birthYear = year - (60 + random.nextInt(10)); // 60 ~ 69 years old
            if (random.nextBoolean()) {
                birthYear += 10; // 70 ~ 79 years old
            }
        }
        int month = random.nextInt(12) + 1; // 1 ~ 12
        int day = random.nextInt(28) + 1;   // 1 ~ 28
        return String.valueOf(birthYear) + String.valueOf(month) + String.valueOf(day);
    }

    private <T> T getRandomElement(T[] array) {
        return array[random.nextInt(array.length)];
    }

}

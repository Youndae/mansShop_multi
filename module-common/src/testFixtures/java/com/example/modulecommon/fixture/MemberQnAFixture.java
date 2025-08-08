package com.example.modulecommon.fixture;

import com.example.modulecommon.model.entity.Member;
import com.example.modulecommon.model.entity.MemberQnA;
import com.example.modulecommon.model.entity.MemberQnAReply;
import com.example.modulecommon.model.entity.QnAClassification;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MemberQnAFixture {

    public static List<MemberQnA> createMemberQnACompletedAnswer(List<QnAClassification> classifications, List<Member> members) {
        List<MemberQnA> result = new ArrayList<>();

        for(Member m : members) {
            for(QnAClassification c : classifications) {
                result.add(createMemberQnA(c, m));
            }
        }

        result.forEach(v -> v.setMemberQnAStat(true));

        return result;
    }

    public static List<MemberQnA> createDefaultMemberQnA(List<QnAClassification> classifications, List<Member> members) {
        return members.stream()
                .flatMap(m ->
                        classifications.stream().map(c -> createMemberQnA(c, m))
                )
                .toList();
    }

    private static MemberQnA createMemberQnA(QnAClassification classification, Member member) {
        return MemberQnA.builder()
                .member(member)
                .qnAClassification(classification)
                .memberQnATitle(member.getUserId() + " QnA Title")
                .memberQnAContent(member.getUserId() + " QnA Content")
                .build();
    }

    public static List<MemberQnAReply> createMemberQnAReply(List<MemberQnA> memberQnAs, Member admin) {
        List<MemberQnAReply> result = new ArrayList<>();
        for(MemberQnA qna : memberQnAs) {
            int count = randomInt();

            for(int i = 0; i < count; i++) {
                Member member = i % 2 == 0 ? admin : qna.getMember();

                result.add(
                        MemberQnAReply.builder()
                                .member(member)
                                .memberQnA(qna)
                                .replyContent(qna.getMemberQnATitle() + " Reply " + i)
                                .build()
                );
            }
        }

        return result;
    }

    private static int randomInt() {
        Random ran = new Random();

        return ran.nextInt(4) + 1;
    }
}

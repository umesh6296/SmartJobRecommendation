package com.example.smartjobrecommendation.utils;

public class SkillMatcher {

    public static int calculateMatch(String userSkills, String jobSkills) {

        if (userSkills == null || jobSkills == null)
            return 0;

        String[] user = userSkills.toLowerCase().split(",");
        String[] job = jobSkills.toLowerCase().split(",");

        int matchCount = 0;

        for (String js : job) {
            for (String us : user) {
                if (js.trim().equals(us.trim())) {
                    matchCount++;
                    break;
                }
            }
        }

        if (job.length == 0)
            return 0;

        return (matchCount * 100) / job.length;
    }
}
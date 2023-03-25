import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This is just a demo for you, please run it on JDK17 (some statements may be not allowed in lower
 * version). This is just a demo, and you can extend and implement functions based on this demo, or
 * implement it in a different way.
 */
public class OnlineCoursesAnalyzer {

    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], info[2].split("/"), info[3], info[4],
                    info[5],
                    Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                    Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                    Double.parseDouble(info[11]),
                    Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                    Double.parseDouble(info[14]),
                    Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                    Double.parseDouble(info[17]),
                    Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                    Double.parseDouble(info[20]),
                    Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
        Map<String, Integer> m = new HashMap<>();
        for (Course c : courses) {
            if (m.containsKey(c.getInstitution())) {
                int count = m.get(c.getInstitution());
                count += c.getParticipants();
                m.replace(c.getInstitution(), count);
            } else {
                m.put(c.getInstitution(), c.getParticipants());
            }

        }

        Map<String, Integer> m1 = m.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(Collectors
                .toMap(Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new));

        return m1;
    }

    //2
    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> m = new HashMap<>();
        for (Course c : courses) {
            if (c.getInstitution() != null && c.getSubject() != null) {
                if (m.containsKey(c.getInstitution() + "-" + c.getSubject())) {
                    int count = m.get(c.getInstitution() + "-" + c.getSubject());
                    count += c.getParticipants();
                    m.replace(c.getInstitution() + "-" + c.getSubject(), count);
                } else {
                    m.put(c.getInstitution() + "-" + c.getSubject(), c.getParticipants());
                }
            }
        }
        Map<String, Integer> m1 = m.entrySet()
            .stream()
            .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
            .collect(Collectors
                .toMap(Map.Entry::getKey,
                    Map.Entry::getValue,
                    (e1, e2) -> e1,
                    LinkedHashMap::new));
        return m1;
    }

    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> m = new HashMap<>();
        for (Course c : courses) {
            String[] s;
            if (c.getInstructors().contains(", ")) {
                s = c.getInstructors().split(", ");
            } else {
                s = new String[]{c.getInstructors()};
            }
            if (s.length == 1) {
                if (m.containsKey(s[0])) {
                    if (!m.get(s[0]).get(0).contains(c.getTitle())) {
                        m.get(s[0]).get(0).add(c.getTitle());
                    }
                } else {
                    List<List<String>> l = new ArrayList<>();
                    List<String> l1 = new ArrayList<>();
                    List<String> l2 = new ArrayList<>();
                    l1.add(c.getTitle());
                    l.add(l1);
                    l.add(l2);
                    m.put(s[0], l);
                }
            } else {
                for (String s1 : s) {
                    if (m.containsKey(s1)) {
                        if (!m.get(s1).get(1).contains(c.getTitle())) {
                            m.get(s1).get(1).add(c.getTitle());
                        }
                    } else {
                        List<List<String>> l = new ArrayList<>();
                        List<String> l1 = new ArrayList<>();
                        List<String> l2 = new ArrayList<>();
                        l2.add(c.getTitle());
                        l.add(l1);
                        l.add(l2);
                        m.put(s1, l);
                    }
                }
            }
        }
        for (String key : m.keySet()) {
            Collections.sort(m.get(key).get(0));
            Collections.sort(m.get(key).get(1));
        }
        return m;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        if (by == "hours") {
            return courses.stream().sorted(Comparator.comparing(Course::getTotalHours).reversed())
                .map(Course::getTitle).distinct().limit(topK).toList();
        } else {
            return courses.stream().sorted(Comparator.comparing(Course::getParticipants).reversed())
                .map(Course::getTitle).distinct().limit(topK).toList();
        }
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited,
        double totalCourseHours) {
        return courses.stream()
            .filter(c -> c.getSubject().toLowerCase().contains(courseSubject.toLowerCase())
                && c.getPercentAudited() >= percentAudited
                && c.getTotalHours() <= totalCourseHours).map(Course::getTitle).distinct().sorted()
            .toList();
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, List<Course>> grp = courses.stream()
            .collect(Collectors.groupingBy(Course::getNumber));
        List<Course2> l = new ArrayList<>();
        for (String key : grp.keySet()) {
            List<Course> temp_c = grp.get(key);
            double a = temp_c.stream().mapToDouble(Course::getMedianAge).average().orElse(0D);
            double m = temp_c.stream().mapToDouble(Course::getPercentMale).average().orElse(0D);
            double b = temp_c.stream().mapToDouble(Course::getPercentDegree).average().orElse(0D);
            double sv = Math.pow(age - a, 2) + Math.pow(gender * 100 - m, 2) + Math.pow(
                isBachelorOrHigher * 100 - b, 2);
            String l_csm = "";
            String[] latest_date = new String[]{"01", "01", "1800"};
            for (Course c : courses) {
                if (Objects.equals(c.getNumber(), key)) {
                    if (Integer.parseInt(latest_date[2]) < Integer.parseInt(c.getLaunchDate()[2])) {
                        latest_date = c.getLaunchDate();
                        l_csm = c.getTitle();
                    } else if (Integer.parseInt(latest_date[2]) == Integer.parseInt(c.getLaunchDate()[2])
                        && Integer.parseInt(latest_date[0]) < Integer.parseInt(c.getLaunchDate()[0])) {
                        latest_date = c.getLaunchDate();
                        l_csm = c.getTitle();
                    } else if (Integer.parseInt(latest_date[2]) == Integer.parseInt(c.getLaunchDate()[2])
                        && Integer.parseInt(latest_date[0]) == Integer.parseInt(c.getLaunchDate()[0])
                        && Integer.parseInt(latest_date[1]) < Integer.parseInt(c.getLaunchDate()[1])) {
                        latest_date = c.getLaunchDate();
                        l_csm = c.getTitle();
                    }
                }
            }
            Course2 temp = new Course2(key, l_csm, sv);
            l.add(temp);
        }

        return l.stream()
            .sorted(Comparator.comparing(Course2::getSv).thenComparing(Course2::getCsTitle))
            .map(Course2::getCsTitle).distinct().limit(10).toList();
    }
}

class Course2 {

    String csNumber;
    String csTitle;
    double sv;

    public Course2(String csNumber, String csTitle, double sv) {
        this.csNumber = csNumber;
        this.csTitle = csTitle;
        this.sv = sv;
    }

    public double getSv() {
        return sv;
    }

    public String getCsNumber() {
        return csNumber;
    }

    public String getCsTitle() {
        return csTitle;
    }
}

class Course {

    String institution;
    String number;
    String[] launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;
    double sv;

    public Course(String institution, String number, String[] launchDate,
        String title, String instructors, String subject,
        int year, int honorCode, int participants,
        int audited, int certified, double percentAudited,
        double percentCertified, double percentCertified50,
        double percentVideo, double percentForum, double gradeHigherZero,
        double totalHours, double medianHoursCertification,
        double medianAge, double percentMale, double percentFemale,
        double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) {
            title = title.substring(1);
        }
        if (title.endsWith("\"")) {
            title = title.substring(0, title.length() - 1);
        }
        this.title = title;
        if (instructors.startsWith("\"")) {
            instructors = instructors.substring(1);
        }
        if (instructors.endsWith("\"")) {
            instructors = instructors.substring(0, instructors.length() - 1);
        }
        this.instructors = instructors;
        if (subject.startsWith("\"")) {
            subject = subject.substring(1);
        }
        if (subject.endsWith("\"")) {
            subject = subject.substring(0, subject.length() - 1);
        }
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
        this.sv = 10000000;
    }

    public String[] getLaunchDate() {
        return launchDate;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public int getYear() {
        return year;
    }

    public String getInstitution() {
        return institution;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getNumber() {
        return number;
    }

    public String getSubject() {
        return subject;
    }

    public String getTitle() {
        return title;
    }

    public double getSv() {
        return sv;
    }
}
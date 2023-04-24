import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Recruit01_jsw {
    // 전형 점수 상수로 만들기

    final int ESMAX = 5;
    final int MGMAX = 3;
    final String[] RECRUIT_DEPT = { "ES개발부", "경영지원실" };
    int ESPass = 0; // ES부서 합격인원
    int MgPass = 0; // 경영지원팀 합격 인원
    int max = 0;
    List<String> listMg = new ArrayList<String>();
    List<String> listES = new ArrayList<String>();
    final String PATH = "C:/Users/NC592/Documents/kaoni_2023_applicants2.csv";
    public String volunteer[];

    public static void main(String[] args) {
        Recruit01_jsw recruit = new Recruit01_jsw();
        if (recruit.reacCSVInfo() == 0) {
            return;
        }
        if (recruit.passRecruit() == 0) {
            return;
        }
    }

    // 지원자정보 읽어오는 메소드
    public int reacCSVInfo() {
        String res = "";
        double Credit = 0; // 학점
        String departments = ""; // 지원부서
        String answer; //
        BufferedReader br = null;
        String dpa = ""; // 정처기 유무
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(PATH), "UTF-8"));
            while ((answer = br.readLine()) != null) {
                String[] arr = answer.split("\t");
                int intvScore = 0;
                int writScore = 0;
                int score = 0;
                for (int i = 0; i < arr.length; i++) {
                    volunteer = answer.split(",");
                    intvScore = Integer.parseInt(volunteer[3]);
                    writScore = Integer.parseInt(volunteer[4]);
                    score = intvScore + writScore;
                    Credit = Double.parseDouble(volunteer[2]);
                    System.out.println("지원부서 : " + volunteer[0]);
                    departments = String.format(volunteer[0]);
                    System.out.println("이름 : " + volunteer[1]);
                    System.out.println("학점 : " + volunteer[2]);
                    System.out.println("면접점수 : " + volunteer[3]);
                    System.out.println("필기점수 : " + volunteer[4]);
                    System.out.println("입사전형 점수 : " + score);
                    System.out.println("정보처리기사 보유 여부 : " + volunteer[5]);
                    dpa = String.format(volunteer[5]);
                    // Result 출력 함수 따로 만들어 main으로 보여질 수 있도록..
                    if (isInputErr(Credit, intvScore, writScore) == 0) {
                        res = "입력값오류";
                    } else {
                        switch (volunteer[0]) {
                            case "ES개발부":
                                PassORFail_ES(dpa, score, Credit);
                                break;
                            case "경영지원실":
                                passMG(score, Credit);
                                break;
                            default:
                                res = "채용 계획 없음";
                                break;
                        }
                    }
                    System.out.println("최종결과 : " + res);
                    max++;

                    if (i != arr.length - 1) {
                        System.out.println("");
                    } else {
                        System.out.println();
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("!!! CSV 읽어오기 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }


    // 입력값오류 메소드
    public int isInputErr(double gpa, int interview_score, int test_score) {
        if (gpa < 0 || gpa > 4.5) {
            return 0;
        } else if (interview_score < 0 || interview_score > 100) {
            return 0;
        } else if (test_score < 0 || test_score > 100) {
            return 0;
        }
        return 1;
    }

    // ES개발부 합격 조건
    public int PassORFail_ES(String dpa, int score, double Credit) {
         String res = "";
        if (ESPass > 5) {
            res = "정원초과";
        } else {
            PassDPA(dpa, score, Credit);
        }
        return 1;
    }

    // ES개발부 합격 불합격 메소드
    public int PassDPA(String dpa, int score, double Credit) {
        String res = "";
        try {
            if (dpa == "Y") {
                List<String> listES = new ArrayList<String>();
                if (score >= 150 && Credit >= 3.0) {
                    res = "합격";
                    ESPass++;
                    listES.add(volunteer[1]);
                } else {
                    res = "불합격";
                }
            } else {
                if (score >= 160 && Credit >= 3.5) {
                    res = "합격";
                    ESPass++;
                    listES.add(volunteer[1]);
                } else {
                    res = "불합격";
                }
            }
            return 1;
        } catch (Exception e) {
            System.out.println("!!! ES개발부 합격 오류 !!!");
            e.printStackTrace();
            return 0;
        }
    }

    // 결과 확인 메서드
    

    // 경영지원실 합격 조건
    public int passMG(int score, double Credit) {
         String res = "";
        if (MgPass > 3) {
            res = "정원초과";
        } else {
            if (score >= 160 && Credit >= 3.7) {
                res = "합격";
                MgPass++;
                listMg.add(volunteer[1]);
            } else {
                res = "불합격";
                return 0;
            }
        }
        return 1;
    }

    // 합격 출력 메소드
    public int passRecruit() {
        try {
            System.out.println("!! 합격발표 !!");
            System.out.println("총 지원자 : " + max);
            System.out.println("ES개발부 합격인원 : " + ESPass);
            for (Object object : listES) {
                String element = (String) object;
                System.out.println("명단 : " + element);
            }
            System.out.println();
            System.out.println("경영지원실 합격인원 : " + MgPass);
            for (Object object : listMg) {
                String element = (String) object;
                System.out.println("명단 : " + element);
            }
            return 1;
        } catch (Exception e) {
            System.out.println("합격 명단 오류");
            e.printStackTrace();
            return 0;
        }
    // 결과 출력 메소드
//    public String resMethod(int gpa, String dpa, int interview_score, int test_score) {
//        String res = "";
//        if(isInputErr(gpa, interview_score, test_score) == 0){
//            res = "입력값오류";
//        }else{
//            switch (volunteer[0]) {
//                case "ES개발부":
//                    PassORFail_ES(dpa, score, Credit);
//                    break;
//                case "경영지원실":
//                    passMG(score, Credit);
//                    break;
//                default:
//                    res = "채용 계획 없음";
//                    break;
//            }
//        }
//        return res;
//    }
    }
}


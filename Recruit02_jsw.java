import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
// UTF-8(BOM)으로 인코딩된 데이터는 2Byte가 나옴??
// subString : Bom 파일은 2byte 2번째거부터...
public class Recruit02_jsw {
    final String[] RECRUIT_DEPT = {"ES개발부","경영지원실"};
    final int ESMAX = 5; // ES개발부 채용인원
    final int MGMAX = 3; // 경영지원실 채용인원
    final String PATH = "C:/Users/NC592/Documents/kaoni_2023_applicants.csv";
    final String URL = "jdbc:mariadb://localhost:3306/test";
    final String USERNAME = "root";
    final String PASSWD = "jsw0101151!";
    public Connection conn;
    public PreparedStatement pstmt;
    public ResultSet rs;
    // removeBomHeader 메소드 -> 메소드 이용해 2byte 제거..

    public static void main(String[] args) throws Exception {
        Recruit02_jsw recruit = new Recruit02_jsw();
        if (recruit.getConnection() == 1) {
            System.out.println();

            // 지원자 정보 읽어오고 DB에 넣는 메소드
            if (recruit.insertApplicantInfo() == 0) {      // 지원자 정보 읽어오고 DB에 넣는 메소드
                recruit.closeDB();
                return;
            }

            // 입력값오류 update
            if (recruit.updateInputCheck() == 0 ) {    // 입력값오류 update
                recruit.closeDB();
                return;
            }

            if (recruit.updateNoPlanCheck() == 0) {      // 채용계획없음 update
                recruit.closeDB();
                return;
            }

            if (recruit.printScan() == 0) {        // 부서확인 >> 합격 update, 합격 출력
                recruit.closeDB();
                return;
            }

            if (recruit.updateOverRecruit() == 0) {    // 정원초과 update checkoverRecuit
                recruit.closeDB();
                return;
            }

            if (recruit.fail_applicants() == 0) {
                recruit.closeDB();
                return;
            }

            System.out.println();
        } else {
            System.out.println("!!! 프로그램 실행 오류 !!!");
            recruit.closeDB();
        }
    }

    // DB Connect 메소드
    public int getConnection() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USERNAME, PASSWD);
            System.out.println(URL + " " + USERNAME +" "+ PASSWD);
            System.out.println("DB 연결 성공");
        } catch (Exception e) {
            System.out.println ("!!! DB 연결 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 최종 성적 확인 메소드
    public double totalScore(Double gpa, int interview_score, int test_score) throws Exception {
        double tot = 0;
        if (inputErr(gpa, interview_score, test_score) == 0) {
            tot = 0;
        } else if (inputErr(gpa, interview_score, test_score) == 1) {
            tot = gpa * 100 / 4.5 + interview_score + test_score;
            tot = Double.parseDouble(String.format("%f", tot));
        }
        return tot;
    }

    // 지원자 정보 읽어오고 DB에 넣는 메소드
    public int insertApplicantInfo() {
        String line = "";
        try (BufferedReader br = new BufferedReader (new InputStreamReader (new FileInputStream (PATH)))) {
            while ((line = br.readLine()) != null) {
                String[] csvdata = line.split(",");
                String department = csvdata[0];
                String name = csvdata[1];
                double gpa = Double.parseDouble(csvdata[2]);
                int interview_score = Integer.parseInt(csvdata[3]);
                int test_score = Integer.parseInt(csvdata[4]);
                double total_score = totalScore(gpa, interview_score, test_score);
                // if return
                insertDB(department, name, gpa, interview_score, test_score, total_score);
            }
        } catch (Exception e) {
            System.out.println("!!! 지원자 정보 읽어오기 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // DB에 삽입
    public int insertDB (String department, String name, Double gpa, int interview_score, int test_score, Double total_score) {
        try {
            String sql = "INSERT INTO tbl_applicant(deptment, name, gpa, interview_score, test_score, total_score) " +
                    "VALUES (?,?,?,?,?,?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, department);
            pstmt.setString(2, name);
            pstmt.setDouble(3, gpa);
            pstmt.setInt(4, interview_score);
            pstmt.setInt(5, test_score);
            pstmt.setDouble(6, total_score);
            pstmt.executeUpdate();
            conn.rollback();
            conn.commit();
//        conn.rollback();
//        conn.commit();
        } catch (Exception e) {
            System.out.println("!!! insertDB 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 입력값오류 >> boolean type
    static int inputErr (double gpa, int interview_score, int test_score) {
        int res;
        if (gpa < 0 || gpa > 4.5) {
            res = 0;
        } else if (interview_score < 0 || interview_score > 100) {
            res = 0;
        } else if (test_score < 0 || test_score > 100) {
            res = 0;
        } else {
            res = 1;
        }
        return res;
    }

    // 입력값오류 update >> updateInputCheck 동사형 . 명사형
    public int updateInputCheck() {
        try{
            String sql = "UPDATE tbl_applicant " +
                    "SET pass_yn = 'N', fail_reason = '입력값오류' " +
                    "WHERE (gpa <= 0 || gpa >= 4.5) " +
                    "OR (interview_score <= 0 || interview_score >= 100) " +
                    "OR (test_score <= 0 || test_score >= 100) " +
                    "AND pass_yn IS NULL";
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e){
            System.out.println("!!! 입력값오류 UPDATE 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 채용계획없음 update >
    public int updateNoPlanCheck() {
        try {
            String sql = "UPDATE tbl_applicant " +
                    "SET pass_yn = 'N', fail_reason = '채용계획없음' " +
                    "WHERE deptment <> 'ES개발부' " +
                    "AND deptment <> '경영지원실' " +
                    "AND pass_yn IS NULL";
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.out.println("!!! 채용계획없음 UPDATE 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 합격 update
    public int updatePassRecruit (String department, int ESMAX, int MGMAX) {
        try {
            if(department.equals("ES개발부") || department.equals("경영지원실")){
                String sql = "UPDATE tbl_applicant " +
                        "SET pass_yn = 'Y', fail_reason = '합격' " +
                        "WHERE deptment = '" + department + "' " +
                        "AND pass_yn IS NULL " +
                        "ORDER BY total_score DESC " +
                        "LIMIT " ;
                if (department.equals("ES개발부")){
                    sql += ESMAX;
                } else if (department.equals("경영지원실")) {
                    sql += MGMAX;
                }
                pstmt = conn.prepareStatement(sql);
                pstmt.executeUpdate();
            }
        } catch (Exception e) {
            System.out.println("!!! 합격 UPDATE 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 정원초과 update
    public int updateOverRecruit() {
        try {
            String sql = "UPDATE tbl_applicant " +
                    "SET pass_yn = 'N' , " +
                    "fail_reason = '정원초과'  " +
                    "WHERE pass_yn IS NULL";
            pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
        } catch (Exception e){
            System.out.println("!!! 정원초과 UPDATE 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 부서 확인 메소드 > printScan
    public int printScan() {
        try {
            for (int i = 0 ; i < RECRUIT_DEPT.length ; i++) {
                updatePassRecruit(RECRUIT_DEPT[i], ESMAX, MGMAX); // 합격 update
                pass_applicants(RECRUIT_DEPT[i]);     // ES개발부 경영지원실 합격
            }
        } catch (Exception e) {
            System.out.println("!!! 부서확인 실패 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // ES개발부 경영지원실 합격명단
    public int pass_applicants(String deptment) {
        try {
            String sql = "Select name, gpa, total_score " +
                    "FROM tbl_applicant " +
                    "where fail_reason = '합격' " +
                    "AND deptment = '"+ deptment +"' " +
                    "order by name asc";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            System.out.println("**** "+ deptment +" 합격자 명단 **** ");
            while (rs.next()) {
                String name = rs.getString("name");
                Double gpa = rs.getDouble("gpa");
                Double totalScore = rs.getDouble("total_score");
                System.out.println(name + " " + gpa + " " + totalScore);
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("!!!"+ deptment +" 합격자 명단 오류 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // 불합격명단
    public int fail_applicants() {
        try {
            String sql = "Select name, gpa, total_score " +
                    "FROM tbl_applicant " +
                    "where pass_yn = 'N' " +
                    "order by name asc";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            System.out.println("**** 불합격자 명단 **** ");
            while (rs.next()) {
                String name = rs.getString("name");
                Double gpa = rs.getDouble("gpa");
                Double totalScore = rs.getDouble("total_score");
                System.out.println(name + " " + gpa + " " + totalScore);
            }
            System.out.println();
        } catch (Exception e) {
            System.out.println("!!! 불합격자 명단 오류 !!!");
            e.printStackTrace();
            return 0;
        }
        return 1;
    }

    // DB 종료 메소드
    public int closeDB(){
        try{
            if (rs != null) {
                rs.close();
            }
            if (pstmt != null) {
                pstmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return 1;
    }

}


package fr.fonkio.importhome;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ImportHome {

    public static Connection conn = getConnection();
    private static String oldWorld;
    private static String newWorld;
    private static String serverName;

    public static void main(String[] args) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("Enter the path :");
        String path = reader.readLine();
        File f = new File(path);
        if (!f.isDirectory()) {
            System.out.println("Not a folder");
            System.exit(0);
        }
        System.out.println("Search in : "+f.getAbsolutePath());
        File[] lf = f.listFiles();
        int nbTot = lf.length;
        System.out.println("There are "+nbTot+" files to read");
        System.out.println("Old world name :");
        ImportHome.oldWorld = reader.readLine();
        System.out.println("New world name :");
        ImportHome.newWorld = reader.readLine();
        System.out.println("Bungee server name :");
        ImportHome.serverName = reader.readLine();
        try {
            ImportHome.conn.setAutoCommit(false);
        } catch (SQLException e1) {
            e1.printStackTrace();
        }


        int prog = 1;
        for(File fi : lf) {
            System.out.println("Reading the file \""+fi.getName()+"\" "+prog+"/"+nbTot +"("+(prog*100/nbTot)+"%)");
            if (fi.getName().contains(".yml")) {
                ImportHome ih = new ImportHome();
                try {
                    ih.readFile(fi);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else
                System.out.println("\t\t\t\""+fi.getName() +"\" not a .yml file !");
            prog ++;

        }

        try {
            ImportHome.conn.commit();
            ImportHome.conn.setAutoCommit(true);
            ImportHome.conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }


    }


    public static Connection getConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("Connection to the MaSuiteHome database :");
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Host :");
            String host = reader.readLine();
            System.out.println("Database name :");
            String dbname = reader.readLine();
            System.out.println("User :");
            String user = reader.readLine();
            System.out.println("Password :");
            String password = reader.readLine();
            System.out.println("Connection in progress ...");
            return DriverManager.getConnection("jdbc:mysql://"+host+"/"+dbname+"?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC", user, password);
        } catch(SQLException e) {
            e.printStackTrace();
            System.out.println("\nFAIL TO CONNECT TO THE DATABASE");

            System.exit(0);
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
            System.out.println("\nFAIL TO CONNECT TO THE DATABASE (CLASSNOTFOUND)");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public void readFile (File f) throws IOException, SQLException {

        Yaml yaml = new Yaml();

        InputStream targetStream = new FileInputStream(f);

        Map<String, Object> obj = yaml.load(targetStream);

        targetStream.close();

        @SuppressWarnings("unchecked")
        Map<String, Object> homes =  (Map<String, Object>) obj.getOrDefault("homes", new HashMap<String, Object>());

        String uuid = f.getName().replace(".yml", "");


        for (String homei: homes.keySet()) {
            Map<String, Object> detailHome =  (Map<String, Object>) homes.getOrDefault(homei, new HashMap<String, Object>());
            System.out.println(homei+" :");
            System.out.println(detailHome.values());
            String sql = "INSERT INTO masuite_homes (name, owner, server, world, x, y, z, yaw, pitch) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmtInsert = ImportHome.conn.prepareStatement(sql);
            stmtInsert.setString(1, homei);
            stmtInsert.setString(2, uuid);
            stmtInsert.setString(3, ImportHome.serverName);
            stmtInsert.setString(4, ((String)detailHome.get("world")).replace(ImportHome.oldWorld, ImportHome.newWorld));
            stmtInsert.setDouble(5, (Double) detailHome.get("x"));
            stmtInsert.setDouble(6, (Double) detailHome.get("y"));
            stmtInsert.setDouble(7, (Double) detailHome.get("z"));
            stmtInsert.setDouble(8, (Double) detailHome.get("yaw"));
            stmtInsert.setDouble(9, (Double) detailHome.get("pitch"));
            stmtInsert.executeUpdate();
            stmtInsert.close();

        }

    }

}

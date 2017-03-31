package org.wtiger.inno.apacheignite;


import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;

import java.sql.*;
import java.util.Random;

/**
 * @author Khayrutdinov Marat
 *         Email: mail@wtiger.org
 *         Created on 31.03.2017.
 */

public class HelloWorld {
    static private int tmp = 0;
    public static void main(String[] args) throws IgniteException, ClassNotFoundException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        try (Ignite ignite = Ignition.start("C:\\Users\\olymp\\" +
                "Downloads\\apache-ignite-fabric-1.9.0-bin\\examples\\config\\example-ignite.xml")) {
            // Put values in cache.
            IgniteCache<Integer, String> cache = ignite.getOrCreateCache("myCache");
            long sum = 0;
            Connection connection = getConPostgres("postgres");
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO somedata(id, \"value\") VALUES (?, ?)");
            Random rnd = new Random();
            for (int i = 1; i < 100000; i++) {
                sum = rnd.nextInt();
                cache.put(i, ""+sum );
//                preparedStatement.setInt(1, i);
//                preparedStatement.setLong(2, sum);
//                preparedStatement.executeUpdate();
            }

            // Get values from cache and
            // broadcast 'Hello World' on all the nodes in the cluster.
            Long prevTime = System.currentTimeMillis();
            ignite.compute().broadcast(() -> {
                for (int i = 1; i < 100000; i++) {
                    String result = cache.get(i);
                    setTmp(result.length());
                }
            });
            System.out.println(System.currentTimeMillis() - prevTime);
            PreparedStatement ps = connection.prepareStatement(
                    "SELECT * from somedata");
            ps.execute();
            ResultSet resultSet = ps.getResultSet();
            prevTime = System.currentTimeMillis();
            while (resultSet.next()){
                String result = resultSet.getString("value");
                setTmp(result.length());
            }
            System.out.println(System.currentTimeMillis() - prevTime);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConPostgres(String user) throws SQLException {
        String password = "hrenasword";
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres",
                user, password);
    }

    public static void setTmp(int tmp) {
        HelloWorld.tmp = tmp;
    }
}
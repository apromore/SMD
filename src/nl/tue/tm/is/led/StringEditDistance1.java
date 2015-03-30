package nl.tue.tm.is.led;

public class StringEditDistance1 {
       public static int editDistance(String a, String b) {
               int[][] ed = new int[a.length()+1][b.length()+1];

               for (int i = 0; i < a.length()+1; i++) {
                       ed[i][0] = i;
               }

               for (int j = 1; j < b.length()+1; j++) {
                       ed[0][j] = j;
                       for (int i = 1; i < a.length()+1; i++) {
                               ed[i][j] = Math.min(ed[i-1][j]+1,
                                                   Math.min(ed[i][j-1]+1,
                                                   ed[i-1][j-1] + (a.charAt(i-1) == b.charAt(j-1) ? 0 : 1)));
                       }
               }

               return ed[a.length()][b.length()];
       }
       public static double similarity(String a, String b){
               double edScore = editDistance(a, b);
               return (edScore == 0 ? 1 :
                       (1 -  edScore/Math.max(a.length(), b.length())));
       }
}
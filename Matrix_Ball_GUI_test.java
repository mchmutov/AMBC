import javax.swing.JFrame;
import javax.swing.UIManager;


public class Matrix_Ball_GUI_test {

	public static void main(String[] args) {
		//try { 
		//    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		//} catch (Exception e) {
		//    e.printStackTrace();
		//}
		JFrame window = new JFrame("Affine Matrix Ball");
		MatrixBallGUI content = new MatrixBallGUI();
		window.setContentPane(content);
		window.setSize(900,600);
		window.setLocation(100,0);
		window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		window.setVisible(true);

	}

}


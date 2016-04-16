package app;


import org.jfree.ui.RefineryUtilities;
import ui.View;

public class App {


	public static void main(String[] args) {
		View dynamicChart = new View("Самописец");
		dynamicChart.pack();
		RefineryUtilities.centerFrameOnScreen(dynamicChart);
		dynamicChart.setVisible(true);

	}

}

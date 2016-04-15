package app;


import org.jfree.ui.RefineryUtilities;

public class App {



	public static void main(String[] args) {
		View dynamicChart = new View("Самописец");
//		dynamicChart.setMCUSamplesPerSecond("15 выборок/с");
//		dynamicChart.setChoosesValuesSamplesPerSecond(4);
		dynamicChart.pack();
		RefineryUtilities.centerFrameOnScreen(dynamicChart);
		dynamicChart.setVisible(true);

	}

}

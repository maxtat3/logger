package app;


import org.jfree.ui.RefineryUtilities;

public class App {



	public static void main(String[] args) {
		DynamicData dynamicChart = new DynamicData("Самописец");
		dynamicChart.uartInit();
		dynamicChart.pack();
		RefineryUtilities.centerFrameOnScreen(dynamicChart);
		dynamicChart.setVisible(true);

	}

}

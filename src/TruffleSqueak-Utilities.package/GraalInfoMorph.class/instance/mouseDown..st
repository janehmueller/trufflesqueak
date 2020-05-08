event handling
mouseDown: anEvent
	samples ifNotNil: [ :s |
		(RLivePlotMorph on: s with: 'ggplot(values, aes(x=x, y=y, color=y)) +
		ggtitle("Graal Compilation Queue") + labs(x="time", y="size") +
		theme(axis.text.x=element_blank()) +
		geom_point(size = .5, show.legend = FALSE) +
		scale_color_gradient(low = "#0091ff", high = "#f0650e")')
			openInHand ]
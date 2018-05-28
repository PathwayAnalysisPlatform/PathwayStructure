library("ggplot2")
library("gridExtra")

path <- "../../../Java/PathwayQuery/"

theplots <-
    lapply(c(1985, 2017), function (year) {
        centralities <-
            read.csv(paste0(path, year, "centralities.csv"),
                     stringsAsFactors=FALSE)
        df <- data.frame(radiality = centralities$Radiality,
                         integration = centralities$Integration)
        df.isolated <- df[df$integration < 0.1 & df$radiality < 0.1, ]
        df.start <- df[df$integration < 0.1 & df$radiality > 0.1, ]
        df.end <- df[df$integration > 0.1 & df$radiality < 0.1, ]
        df.main <- df[df$integration > 0.1 & df$radiality > 0.1, ]
        ggplot() +
            geom_point(aes(integration, radiality),
                       df.isolated, col = 'red', shape = 5) +
            geom_point(aes(integration, radiality),
                       df.start, col = 'blue', shape = 2) +
            geom_point(aes(integration, radiality),
                       df.end, col = 'purple', shape = 6) +
            geom_point(aes(integration, radiality),
                       df.main, col = 'darkgreen', shape = 1) +
            geom_abline(col = 'lightgray') +
            scale_x_continuous(limits = c(0,1)) +
            scale_y_continuous(limits = c(0,1)) +
            annotate("text", label=year,
                     x=0.125, y=1,
                     fontface="bold")
    })
grid.arrange(theplots[[1]], theplots[[2]], ncol=2)
ggsave("Figure7.pdf",
       plot = grid.arrange(theplots[[1]], theplots[[2]], ncol=2),
       height=10, width=20, units="cm")



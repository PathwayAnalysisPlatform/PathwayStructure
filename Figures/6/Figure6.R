library("ggplot2")
library("gridExtra")

path <- "../../../../../../PathwayProjectQueries/Java/PathwayQuery/"

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
            geom_abline(col = 'lightgray') +
            geom_point(aes(integration, radiality),
                       df.isolated, col = 'red', shape = 5) +
            geom_point(aes(integration, radiality),
                       df.start, col = 'blue', shape = 2) +
            geom_point(aes(integration, radiality),
                       df.end, col = 'purple', shape = 6) +
            geom_point(aes(integration, radiality),
                       df.main, col = 'darkgreen', shape = 1) +
            scale_x_continuous(limits = c(0,1), name = "Integration") +
            scale_y_continuous(limits = c(0,1), name = "Radiality") +
            annotate("text", label=year,
                     x=0.125, y=1,
                     fontface="bold") +
            theme_set(theme_bw(base_size = 11))
    })

grid.arrange(theplots[[1]], theplots[[2]], ncol=2)

ggsave("Figure6.pdf",
       plot = grid.arrange(theplots[[1]], theplots[[2]], ncol=2),
       height=4, width=8, units="in")

ggsave("Figure6aM.pdf",
       plot = theplots[[1]],
       height=8.5, width=8.5, units="cm")
ggsave("Figure6bM.pdf",
       plot = theplots[[2]],
       height=8.5, width=8.5, units="cm")

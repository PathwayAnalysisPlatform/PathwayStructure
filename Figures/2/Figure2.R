## library(extrafont)
## font_import(pattern = "arial")
##  +    theme(text=element_text(family="Arial", size=12))
library("ggplot2")
library("gridExtra")

path <- "../../resources/centralities/"

dPerYear <- rep(NA, length(1934:2017))

{
    nedgesPerYear <- rep(NA, length(1934:2017))
    cat("1934")
    for (year in 1934:2017) {
        cat("\b\b\b\b")
        cat(year)
        inFile <- paste0(path, year, "lengthShortestPaths.csv")
        dPerYear[year-1933] <- max(as.matrix(read.csv(inFile, row.names=1)))
        nedgesPerYear[year-1933] <-
            sum(as.matrix(read.csv(inFile, row.names=1)) == 1)
    }
    cat("\n")
}

{
    nprotPerYear <- rep(NA, length(1934:2017))
    cat("1934")
    for (year in 1934:2017) {
        cat("\b\b\b\b")
        cat(year)
        nprotPerYear[year-1933] <-
            nrow(read.csv(paste0(path, year, "centralities.csv")))
    }
    cat("\n")
}

## Figure 2
df2 <- data.frame(proteins = nprotPerYear, edges = nedgesPerYear)
offset <- 500
fig2 <-
    ggplot(df2, aes(proteins, edges)) +
    theme_bw() +
    geom_point(col=rgb(137, 208, 245, maxColorValue=250)) +
    geom_line(col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_x_continuous(name = "# Proteins",
                       breaks = c(0, 2500, 5000, 7500)) +
    scale_y_continuous(name = "# Interactions",
                       breaks = c(0, 250000, 500000, 750000),
                       labels = c(0, "250k", "500k", "750k")) +
    annotate("text", label="1984",
             x=nprotPerYear[1984 - 1933] - offset,
             y=nedgesPerYear[1984 - 1933],
             fontface="italic") +
    annotate("text", label="1988",
             x=nprotPerYear[1988 - 1933] - offset,
             y=nedgesPerYear[1988 - 1933],
             fontface="italic") +
    annotate("text", label="1991",
             x=nprotPerYear[1991 - 1933] - offset,
             y=nedgesPerYear[1991 - 1933],
             fontface="italic") +
    annotate("text", label="1994",
             x=nprotPerYear[1994 - 1933] - offset,
             y=nedgesPerYear[1994 - 1933],
             fontface="italic") +
    annotate("text", label="2003",
             x=nprotPerYear[2003 - 1933] - offset,
             y=nedgesPerYear[2003 - 1933],
             fontface="italic") +
    annotate("text", label="2013",
             x=nprotPerYear[2013 - 1933] - offset,
             y=nedgesPerYear[2013 - 1933],
             fontface="italic") +
    annotate("text", label="A", x=0, y=750000,
             fontface="bold", hjust="right", vjust = "bottom") # + theme(aspect.ratio=1)
fig2


## Figure 3
df3 <- data.frame(year = 1934:2017,
                 degree = nedgesPerYear / nprotPerYear)
fig3 <-
    ggplot(df3, aes(year, degree)) +
    theme_bw() +
    geom_point(col=rgb(137, 208, 245, maxColorValue=250)) +
    geom_line(col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Interactions / # Proteins") +
    scale_x_continuous(name = "Year") +
    annotate("text", label="B", x=1934, y=135,
             fontface="bold", hjust="right", vjust = "bottom") # + theme(aspect.ratio=1)


grid.arrange(fig2, fig3, ncol=2)

ggsave("Figure2a.pdf", fig2,
       width=8.5, height=8, units="cm")
ggsave("Figure2b.pdf", fig3,
       width=8.5, height=8, units="cm")

## get the connections from Francisco's code
connectionsFile <- "ProteomeGraph.sif"
fileOutputList <- list()
con <- file(connectionsFile, "r")
while ( TRUE ) {
    line <- readLines(con, n = 1)
    if ( length(line) == 0 ) {
        break
    }
    theLine <- strsplit(line, " ")[[1]]
    theNode <- theLine[1]
    typeConn <- theLine[2]
    fileOutputList[[typeConn]][[theNode]] <-
        theLine[3:length(theLine)]
}
close(con)

names(fileOutputList)
## "ci" "co" "cn" "io" "ri" "ro"
for (name in names(fileOutputList)) {
    print(paste(name,
                length(fileOutputList[[name]])))
}
## catalyst input
## "ci 4127"
## catalyst output
## "co 4121"
## complex neighbour
## "cn 6150"
## input output
## "io 7186"
## regulator input
## "ri 1097"
## regulator output
## "ro 1322"

allNames <-
    unique(
        unlist(
            lapply(names(fileOutputList),
                   function (x) {
                       names(fileOutputList[[x]])
                   })))
length(allNames)
## 8259 proteins with at least one connection

connections <- list()
for (name in allNames) {
    temp <- c()
    for (x in names(fileOutputList)) {
        temp <- c(temp, fileOutputList[[x]][[name]])
    }
    connections[[name]] <- temp
}

## 6 proteins with > 1900 connections
which(unlist(lapply(connections, length)) > 1900)
length(connections[["P63211"]])
length(connections[["P62873"]])
length(connections[["P62987"]])
length(connections[["P62979"]])
length(connections[["P0CG48"]])
length(connections[["P0CG47"]])

length(connections[["P63211"]])
length(unique(connections[["P63211"]]))

conun <- lapply(connections, unique)
plot(density(sapply(connections, length)))
lines(density(sapply(conun, length)), col='red')
abline(v=100, col='lightgray')

## this makes a vector from all the lengths
## 'connections' is a list with vectors containing
## the names of the proteins to which the protein is connected
conlen <- unlist(lapply(connections, length))
sum(conlen == 0) # 0
sum(conlen == 1) # 201
sum(conlen == 2) # 328
sum(conlen == 3) # 162
sum(conlen == 4) # 222
sum(conlen == 5) # 123
sum(conlen > 50)  # 4795
sum(conlen < 100)  # 4537
sum(conlen > 100)  # 3707
sum(conlen > 1000) # 56
sum(conlen > 1500) # 14
sum(conlen > 2000) # 5
allNames[which(conlen > 1500)]
allNames[which(conlen > 2000)]
min(conlen)
max(conlen)
length(conlen)

conunlen <- unlist(lapply(conun, length))
sum(conunlen == 0) # 0
sum(conunlen == 1) # 451
sum(conunlen == 2) # 318
sum(conunlen == 3) # 266
sum(conunlen == 4) # 249
sum(conunlen == 5) # 204
sum(conunlen > 50)  # 3793
sum(conunlen < 100)  # 5315
sum(conunlen > 100)  # 2939
sum(conunlen > 250)  # 1159
sum(conunlen > 1000)  # 2
max(conunlen) # 1058

head(sort(conunlen, decreasing=TRUE), n=10)


conlenHV <- hist(conlen, breaks=seq(0, max(conlen)+1, 1),
     plot=FALSE)
plot(conlenHV$mids[! conlenHV$counts == 0],
     conlenHV$counts[! conlenHV$counts == 0]+1,
     type="h",
     xlab="# Connections To Other Proteins",
     ylab="# Proteins",
     main="",
     lwd=2, lend=2,
     axes=FALSE)
axis(1, at = seq(0, 3000, by=500),
     labels = seq(0, 3000, by=500))
axis(2, at = c(1, 50, 100, 150, 200, 250, 300, 350)+1,
     labels = c(1, 50, 100, 150, 200, 250, 300, 350))
arrows(600, 250, 340, 250, 1/8)
text(600, 250, "Mainly Zinc Fingers", pos=4)
arrows(680, 340, 428, 340, 1/8)
text(680, 340, "Mainly Olfactory Receptors", pos=4)
arrows(2691, 120, 2691, 3, 1/8)
text(2691, 120, "Ubiquitin-40S ribosomal protein S27a", pos=2)
arrows(2642, 100, 2642, 3, 1/8)
text(2642, 100, "Ubiquitin-40S ribosomal protein L40", pos=2)
arrows(2061, 70, 2061, 3, 1/8)
text(2061, 70, "Polyubiquitin-C", pos=2)
arrows(2060, 50, 2060, 3, 1/8)
text(2060, 50, "Polyubiquitin-B", pos=2)
## NEXT TWO ARE GUANININE NUCLEOTIDE BINDING PROTEINS
abline(v=75, col="pink")


library("ggplot2")

ggplot(data.frame(counts=conlen), aes(counts)) +
        geom_bar(fill=rgb(137, 208, 245, maxColorValue=250),
             col=rgb(137, 208, 245, maxColorValue=250)) +
    scale_y_continuous(name = "# Proteins") +
scale_x_continuous(name = "# Connections to other proteins") +
annotate("text", label="Ubiquitin-40S ribosomal protein S27a ",
         x=2691, y=120, hjust="right") +
    annotate("segment",
             x=2691, y=120, xend=2691, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Ubiquitin-40S ribosomal protein L40 ",
         x=2642, y=100, hjust="right") +
    annotate("segment",
             x=2642, y=100, xend=2642, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Polyubiquitin-C ",
         x=2061, y=70, hjust="right") +
    annotate("segment",
             x=2061, y=70, xend=2061, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm"))) +
annotate("text", label="Polyubiquitin-B ",
         x=2060, y=50, hjust="right") +
    annotate("segment",
             x=2060, y=50, xend=2060, yend=2, size=0.5,
             arrow=arrow(length=unit(.2, "cm")))

ggsave("FigureS4.pdf", width=174, height=82, units="mm")

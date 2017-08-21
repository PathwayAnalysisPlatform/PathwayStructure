## for each year
## make two data frames
## vertices => one column : id
## edges => three columns : from, to, type
for (year in 1934:2017) {
    print(year)
    adjList <- list()
    ## will be fileOutputList[[typeConnection]][[startNode]]
    inFile <- paste0("../../Java/PathwayQuery/", year, ".sif")
    adjList <- tryCatch({
        con <- file(inFile, "r")
        while ( TRUE ) {
            line <- readLines(con, n = 1)
            if ( length(line) == 0 ) {
                break
            }
            theLine <- strsplit(line, " ")[[1]]
            theNode <- theLine[1]
            typeConn <- theLine[2]
            adjList[[typeConn]][[theNode]] <- list()
            adjList[[typeConn]][[theNode]] <-
                theLine[3:length(theLine)]
        }
        adjList },
        finally={
            close(con)
        })

    verticesYear <- union(names(adjList[["io"]]),
                          unique(unlist(sapply(adjList[["io"]], unlist))))
    edgesYear <-
        data.frame(from = unlist(lapply(
                       names(adjList[["io"]]),
                       function (x) {
                           rep(x, length(adjList[["io"]][[x]]))
                       })),
                   to = unlist(adjList[["io"]]),
                   type = rep("io", length(unlist(adjList[["io"]]))))

    inoutYear <- graph_from_data_frame(d = unique(edgesYear),
                                       vertices = verticesYear,
                                       directed = T)

    l <- layout_with_fr(inoutYear)
    png(paste0(year, ".png"), width = 800, height = 600)
    plot(inoutYear,
         vertex.shape = "none",
         vertex.label = NA,
         edge.color = defaultColor,
         layout = l)
    dev.off()
}

path <- "../../Java/PathwayQuery/"
dPerYear <- rep(NA, length(1934:2017))
nverticesPerYear <- rep(NA, length(1934:2017))
for (year in 1934:2017) {
    print(year)
    inFile <- paste0(path, year, "lengthShortestPaths.csv")
    dPerYear[year-1933] <- max(as.matrix(read.csv(inFile, row.names=1)))
    nverticesPerYear[year-1933] <-
        sum(as.matrix(read.csv(inFile, row.names=1)) == 1)
}

nprotPerYear <- rep(NA, length(1934:2017))
for (year in 1934:2017) {
    print(year)
    inFile <- paste0(path, year, "centralities.csv")
    nprotPerYear[year-1933] <- nrow(read.csv(inFile))
}


plot(dPerYear)

pdf('diameterAndDensity.pdf', width=14, height=7)
par(mfrow=c(1,2))
plot(dPerYear,
     main="Diameter per year",
     xlab="Year", ylab="Diameter",
     axes=FALSE)
axis(1,
     labels=c(1, 17, 37, 57, 77)+1933,
     at=c(1, 17, 37, 57, 77))
axis(2,
     labels=seq(2, 14, by=2),
     at=seq(2, 14, by=2))
plot(nverticesPerYear/nprotPerYear,
     main='Graph density',
     ylab="# Vertices / # Nodes", xlab="Year",
     axes=FALSE)
axis(1,
     labels=c(1, 17, 37, 57, 77)+1933,
     at=c(1, 17, 37, 57, 77))
axis(2,
     labels=seq(0, 120, by=20),
     at=seq(0, 120, by=20))
dev.off()

pdf('numberProteinsAndVertices.pdf', width=14, height=7)
par(mfrow=c(1,2))
plot(nprotPerYear,
     main='# Proteins per year',
     ylab="# Proteins", xlab="Year",
     ylim=c(0, 755000),
     axes=FALSE)
axis(1,
     labels=c(1, 17, 37, 57, 77)+1933,
     at=c(1, 17, 37, 57, 77))
axis(2,
     labels=c(0, 200000, 500000, 700000),
     at=c(0, 2e5, 4e5, 7e5))
plot(nverticesPerYear,
     main='# Vertices per year',
     ylab="# Vertices", xlab="Year",
     ylim=c(0, 755000),
     axes=FALSE)
axis(1,
     labels=c(1, 17, 37, 57, 77)+1933,
     at=c(1, 17, 37, 57, 77))
axis(2,
     labels=c(0, 200000, 500000, 700000),
     at=c(0, 2e5, 4e5, 7e5))
dev.off()

nverticesPerYear[(2010:2017)-1933]

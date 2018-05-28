path <- "../../../Java/PathwayQuery/"

years <- 1934:2017

maxIn <- 0
maxInYear <- 0
maxOut <- 0
maxOutYear <- 0
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    if (max(centralities$kOut) > maxOut) {
        maxOut <- max(centralities$kOut)
        maxOutYear <- year
    }
    if (max(centralities$kIn) > maxIn) {
        maxIn <- max(centralities$kIn)
        maxInYear <- year
    }
}

for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)

    kOutI <- sort(unique(centralities$kOut))
    kOutN <- length(centralities$kOut)
    kOutF <- rep(NA, length(kOutI))
    for (i in seq_len(length(kOutI))) {
        kOutF[i] <- sum(centralities$kOut == kOutI[i])/kOutN
    }
    kInI <- sort(unique(centralities$kIn))
    kInN <- length(centralities$kIn)
    kInF <- rep(NA, length(kInI))
    for (i in seq_len(length(kInI))) {
        kInF[i] <- sum(centralities$kIn == kInI[i]) /kInN
    }
    pdf(paste0(year, "InOutDegreeHy.pdf"), width=14, height=7)
    par(mfrow=c(1,2))
    plot(kInI, kInF, log='xy',
         main = "Node In-Degree Distribution",
         ylab = "p(k)", xlab = "k",
         ylim = c(0.0001, 1),
         xlim = c(1, 2000),
         axes = FALSE)
    yticks <- c("0.0001", "0.0005", "0.001",
                "0.005", "0.01", "0.05", "0.1", "0.5", "1")
    xticks <- c("1", "5", "10", "50",
                "100", "500", "1000")
    axis(2, at=as.numeric(yticks), labels=yticks, las=1)
    axis(1, at=as.numeric(xticks), labels=xticks)
    plot(kOutI, kOutF, log='xy', col='magenta',
         main = "Node Out-Degree Distribution",
         ylab = "p(k)", xlab = "k",
         ylim = c(0.0001, 1),
         xlim = c(1, 2000),
         pch = 2,
         axes = FALSE)
    axis(2, at=as.numeric(yticks), labels=yticks, las=1)
    axis(1, at=as.numeric(xticks), labels=xticks)
    dev.off()
}

pdf(paste0("AllYears", "InOutDegreeHy.pdf"), width=14, height=7)
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    kOutI <- sort(unique(centralities$kOut))
    kOutN <- length(centralities$kOut)
    kOutF <- rep(NA, length(kOutI))
    for (i in seq_len(length(kOutI))) {
        kOutF[i] <- sum(centralities$kOut == kOutI[i])/kOutN
    }
    kInI <- sort(unique(centralities$kIn))
    kInN <- length(centralities$kIn)
    kInF <- rep(NA, length(kInI))
    for (i in seq_len(length(kInI))) {
        kInF[i] <- sum(centralities$kIn == kInI[i]) /kInN
    }
    par(mfrow=c(1,2))
    plot(kInI, kInF, log='xy',
         main = paste(year, "Node In-Degree Distribution"),
         ylab = "p(k)", xlab = "k",
         ylim = c(0.0001, 1),
         xlim = c(1, 2000),
         axes = FALSE)
    yticks <- c("0.0001", "0.0005", "0.001",
                "0.005", "0.01", "0.05", "0.1", "0.5", "1")
    xticks <- c("1", "5", "10", "50",
                "100", "500", "1000")
    axis(2, at=as.numeric(yticks), labels=yticks, las=1)
    axis(1, at=as.numeric(xticks), labels=xticks)
    plot(kOutI, kOutF, log='xy', col='magenta',
         main = paste(year, "Node Out-Degree Distribution"),
         ylab = "p(k)", xlab = "k",
         ylim = c(0.0001, 1),
         xlim = c(1, 2000),
         pch = 2,
         axes = FALSE)
    axis(2, at=as.numeric(yticks), labels=yticks, las=1)
    axis(1, at=as.numeric(xticks), labels=xticks)
}
dev.off()


for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    kInFakeLog <- ifelse(centralities$kIn == 0, 0.0001, centralities$kIn)
    kOutFakeLog <- ifelse(centralities$kOut == 0, 0.0001, centralities$kOut)
    pdf(paste0(year, "InVsOutDegreeHy.pdf"), width=7, height=7)
    plot(jitter(kInFakeLog), jitter(kOutFakeLog),
         xlab = "In-degree", ylab = "Out-degree", log='xy',
         ylim = c(1, 2000),
         xlim = c(1, 2000),
         axes = FALSE, col=rgb(0,0,0,0.25))
    xyticks <- c("1", "5", "10", "50",
                 "100", "500", "1000")
    axis(2, at=as.numeric(xyticks), labels=xyticks, las=1)
    axis(1, at=as.numeric(xyticks), labels=xyticks)
    abline(0,1,col='lightgray')
    dev.off()
}


pdf(paste0("AllYears", "InVsOutDegreeHy.pdf"), width=7, height=7)
for (year in years) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    kInFakeLog <- ifelse(centralities$kIn == 0, 0.0001, centralities$kIn)
    kOutFakeLog <- ifelse(centralities$kOut == 0, 0.0001, centralities$kOut)
    plot(jitter(kInFakeLog), jitter(kOutFakeLog),
         main = year,
         xlab = "In-degree", ylab = "Out-degree", log='xy',
         ylim = c(1, 2000),
         xlim = c(1, 2000),
         axes = FALSE, col=rgb(0,0,0,0.25))
    xyticks <- c("1", "5", "10", "50",
                 "100", "500", "1000")
    axis(2, at=as.numeric(xyticks), labels=xyticks, las=1)
    axis(1, at=as.numeric(xyticks), labels=xyticks)
    abline(0,1,col='lightgray')
}
dev.off()


pdf(paste0("YearSelection", "InVsOutDegreeHy.pdf"), width=9, height=6)
par(mfrow=c(2,3))
for (year in c(1934, 1960, 1970, 1985, 2000, 2017)) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    kInFakeLog <- ifelse(centralities$kIn == 0, 0.0001, centralities$kIn)
    kOutFakeLog <- ifelse(centralities$kOut == 0, 0.0001, centralities$kOut)
    plot(jitter(kInFakeLog), jitter(kOutFakeLog),
         main = year,
         xlab = "In-degree", ylab = "Out-degree", log='xy',
         ylim = c(1, 2000),
         xlim = c(1, 2000),
         axes = FALSE, col=rgb(0,0,0,0.25))
    xyticks <- c("1", "10", "100", "1000")
    axis(2, at=as.numeric(xyticks), labels=xyticks, las=1)
    axis(1, at=as.numeric(xyticks), labels=xyticks)
    abline(0,1,col='lightgray')
}
dev.off()





## FROM
## http://sas-and-r.blogspot.no/2012/09/example-103-enhanced-scatterplot-with.html
zones <- matrix(c(1,1,1,
                  0,5,0,
                  2,6,4,
                  0,3,0), ncol = 3, byrow = TRUE)
layout(zones, widths=c(0.3,4,1), heights = c(1,3,10,.75))
layout.show(n=6)

scatterhist <- function(x, y, xlab = "", ylab = "", plottitle="",
                        xsize=1, cleanup=TRUE,...){
    ## save the old graphics settings-- they may be needed
    def.par <- par(no.readonly = TRUE)

    zones <- matrix(c(1,1,1, 0,5,0, 2,6,4, 0,3,0), ncol = 3, byrow = TRUE)
    layout(zones, widths=c(0.3,4,1), heights = c(1,3,10,.75))

    ## tuning to plot histograms nicely
    xhist <- hist(x, plot = FALSE)
    yhist <- hist(y, plot = FALSE)
    top <- max(c(xhist$density, yhist$density))

    ## for all three titles:
    ##   drop the axis titles and omit boxes, set up margins
    par(xaxt="n", yaxt="n",bty="n",  mar = c(.3,2,.3,0) +.05)
    ## fig 1 from the layout
    plot(x=1,y=1,type="n",ylim=c(-1,1), xlim=c(-1,1))
    text(0,0,paste(plottitle), cex=2)
    ## fig 2
    plot(x=1,y=1,type="n",ylim=c(-1,1), xlim=c(-1,1))
    text(0,0,paste(ylab), cex=1.5, srt=90)
    ## fig 3
    plot(x=1,y=1,type="n",ylim=c(-1,1), xlim=c(-1,1))
    text(0,0,paste(xlab), cex=1.5)

    ## fig 4, the first histogram, needs different margins
    ## no margin on the left
    par(mar = c(2,0,1,1))
    barplot(yhist$density, axes = FALSE, xlim = c(0, top),
            space = 0, horiz = TRUE)
    ## fig 5, other histogram needs no margin on the bottom
    par(mar = c(0,2,1,1))
    barplot(xhist$density, axes = FALSE, ylim = c(0, top), space = 0)
    ## fig 6, finally, the scatterplot-- needs regular axes, different margins
    par(mar = c(2,2,.5,.5), xaxt="s", yaxt="s", bty="n")
    ## this color allows traparency & overplotting-- useful if a lot of points
    plot(x, y , pch=19, col="#00000022", cex=xsize, ...)

    ## reset the graphics, if desired
    if(cleanup) {par(def.par)}
}

pdf(paste0("YearSelection", "InVsOutDegreeScatHist.pdf"), width=5, height=5)
for (year in c(1934, 1960, 1970, 1985, 2000, 2017)) {
    centralities <-
        read.csv(paste0(path, year, "centralities.csv"),
                 stringsAsFactors=FALSE)
    kInFakeLog <- ifelse(centralities$kIn == 0, 0.0001, centralities$kIn)
    kOutFakeLog <- ifelse(centralities$kOut == 0, 0.0001, centralities$kOut)
    scatterhist(jitter(kInFakeLog), jitter(kOutFakeLog),
         plottitle = year,
         xlab = "In-degree", ylab = "Out-degree", log='xy',
         ylim = c(1, 2000),
         xlim = c(1, 2000),
         cleanup = FALSE)
    abline(0,1,col='lightgray')
}
dev.off()

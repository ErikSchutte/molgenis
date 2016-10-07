load("~/Documents/data/rpkm123leidenfiltered Thu Oct  6 09:29:39 2016.RData")
write.csv(rpkm.filtered,file="~/Desktop/LncRNA Explorer - Uploads/LncRNA_Explorer_Data/rpkm_Tcell_96samples_nonorway_nona.csv",
          col.names = F,row.names = T)

## Some ordering for a gene positions file, keep!
genepos <- read.table("~/Dropbox/Erik/genepos.txt",header = T)
genepos <- genepos[order(genepos[,1]),]
i <- sapply(genepos, is.factor)
genepos[i] <- lapply(genepos[i], as.character)
newchr <- sapply(genepos[,2],function(x) strsplit(x,split="chr")[[1]][2])
for ( i in 1:nrow(genepos)) {
  genepos[i,2] <- newchr[[i]]
}


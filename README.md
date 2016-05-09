# WebCrawler
Project includes:
WebCrawler using the JSoup Library.
PageCleaner which removes noise by finding the part of the page with a lower ratio of non-tag tokens/tag tokens.
Analyzer (hardcoded filepath) which will count each unique word in all of the pages given to it. For use with Zipf's distribution analysis.


The program is initialized with a user selected .csv specification file (seed URL, number of pages to crawl, optional domain restriction).
The crawler allows for multi-threading with a user specified number of spider threads and saves downloaded pages into a user-specified 
 filepath in a folder "repository."
Saved files will be in the format ".html" and will have most image links as well as all information outside of the body tags,
 excepting the title of the page are removed.

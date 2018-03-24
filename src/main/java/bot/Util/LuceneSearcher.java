package bot.Util;

import bot.entity.QaEntity;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.List;

@Repository
public class LuceneSearcher {

    private static Logger logger = Logger.getLogger(LuceneSearcher.class);

    private Directory directory;

    private IndexReader indexReader;

    private IndexSearcher indexSearcher;

    @Autowired
    private QaDataUtil qaDataUtil;

    @PostConstruct
    public void setUp() throws IOException {
        //索引存放的位置，设置在当前目录中
        directory = FSDirectory.open(Paths.get("indexDir/"));
        //创建索引的读取器
        indexReader = DirectoryReader.open(directory);
        //创建一个索引的查找器，来检索索引库
        indexSearcher = new IndexSearcher(indexReader);
    }

    private void init(){
        List<QaEntity> allQaEntities = qaDataUtil.getAllQaEntitys();
        for(QaEntity qaEntity : allQaEntities) {
            //TODO
        }
    }

    @PreDestroy
    public void tearDown() throws Exception {
        indexReader.close();
    }

    /**
     * 执行查询，并打印查询到的记录数
     * @param query
     * @throws IOException
     */
    public void executeQuery(Query query) throws IOException {
        TopDocs topDocs = indexSearcher.search(query, 5);

        //打印查询到的记录数
        logger.info("总共查询到" + topDocs.totalHits + "个文档");
        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            //取得对应的文档对象
            Document document = indexSearcher.doc(scoreDoc.doc);
            logger.info("id：" + document.get("id"));
            logger.info("title：" + document.get("title"));
            logger.info("content：" + document.get("content"));
        }
    }

    /**
     * 分词打印
     *
     * @param analyzer
     * @param text
     * @throws IOException
     */
    public void printAnalyzerDoc(Analyzer analyzer, String text) throws IOException {

        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(text));
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                logger.info(charTermAttribute.toString());
            }
            tokenStream.end();
        } finally {
            tokenStream.close();
            analyzer.close();
        }
    }
}

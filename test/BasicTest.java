import org.junit.*;
import java.util.*;
import play.test.*;
import models.*;

public class BasicTest extends UnitTest {

    @Test
   // public void aVeryImportantThingToTest() {
    //    assertEquals(2, 1 + 1);
   // }
    public void createAndRetrieveUser(){
    	new User("bob@gmail.com","secret","Bob").save(); //create new user and save
    	User bob=User.find("byEmail","bob@gmail.com").first();
    	
    	assertNotNull(bob);
    	assertEquals("Bob",bob.fullname);
    }
    
    @Test
    public void tryConnectAsUser(){
    	new User("bob@gmail.com","secret","Bob").save();
    	
    	assertNotNull(User.connect("bob@gmail.com", "secret"));
    	assertNull(User.connect("bob@gmail.com", "badpassword"));
    	assertNull(User.connect("tom@gmail.com", "secret"));
    }
    
    @Before
    public void setup(){
    	Fixtures.deleteDatabase();
    }
    
    @Test
    public void createPost(){
    	User bob=new User("bob@gmail.com","secret","Bob").save(); //new user
    	new Post(bob,"My first post","Hello world").save();  //new post
    	assertEquals(1,Post.count());  //test the post
    	List<Post>bobPosts=Post.find("byAuthor",bob).fetch();
    	
    	assertEquals(1,bobPosts.size());
    	Post firstPost=bobPosts.get(0);
    	assertNotNull(firstPost);
    	assertEquals(bob,firstPost.author);
    	assertEquals("My first post", firstPost.title);
        assertEquals("Hello world", firstPost.content);
        assertNotNull(firstPost.postedAt);
    	
    }
    
    @Test
    public void postComments(){
    	User bob = new User("bob@gmail.com", "secret", "Bob").save();
    	Post bobPost = new Post(bob, "My first post", "Hello world").save();
    	
    	new Comment(bobPost,"Jeff","Nice Post").save();
    	new Comment(bobPost,"Tom","I knew that!").save();
    	
    	List<Comment> bobPostComments=Comment.find("byPost", bobPost).fetch();
    	assertEquals(2,bobPostComments.size());
    	
    	Comment firstComment=bobPostComments.get(0);
    	assertNotNull(firstComment);
    	assertEquals("Jeff",firstComment.author);
    	assertEquals("Nice Post",firstComment.content);
    	assertNotNull(firstComment.postedAt);
    	
    	Comment secondComment=bobPostComments.get(1);
    	assertNotNull(secondComment);
    	assertEquals("Tom",secondComment.author);
    	assertEquals("I knew that!",secondComment.content);
    	assertNotNull(secondComment.postedAt);
    }
    
    @Test
    public void useTheCommentsRelation(){
    	User bob = new User("bob@gmail.com", "secret", "Bob").save();
    	Post bobPost = new Post(bob, "My first post", "Hello world").save();
    	
    	bobPost.addComment("Jeff","Nice Post");
    	bobPost.addComment("Tom","I knew that !");
    	
    	assertEquals(1,User.count());  
    	assertEquals(1,Post.count());
    	assertEquals(2,Comment.count());
    	
    	bobPost=Post.find("byAuthor", bob).first(); 
    	assertNotNull(bobPost);
    	
    	assertEquals(2,bobPost.comments.size()); 
    	assertEquals("Jeff",bobPost.comments.get(0).author);
    	
    	bobPost.delete(); 
    	
    	assertEquals(1,User.count());  
    	assertEquals(0,Post.count());
    	assertEquals(0,Comment.count());
    }
    
    @Test
    public void fullTest(){
    	Fixtures.loadModels("data.yml");
    	
    	assertEquals(2,User.count());  
    	assertEquals(3,Post.count());
    	assertEquals(3,Comment.count());
    	
    	assertNotNull(User.connect("bob@gmail.com", "secret"));
    	assertNotNull(User.connect("jeff@gmail.com", "secret"));
    	assertNull(User.connect("bob@gmail.com", "badpassword"));
    	assertNull(User.connect("tom@gmail.com", "secret"));
    	
    	List<Post> bobPosts=Post.find("author.email", "bob@gmail.com").fetch();
    	assertEquals(2,bobPosts.size());
    	
    	List<Post> bobComments=Comment.find("post.author.email", "bob@gmail.com").fetch();
    	assertEquals(3,bobComments.size());
    	
    	Post frontPost=Post.find("order by postedAt desc").first();
    	assertNotNull(frontPost);
    	assertEquals("About the model layer",frontPost.title);
    	
    	assertEquals(2,frontPost.comments.size());
    	
    	frontPost.addComment("Jim", "Hello guys");
    	assertEquals(3,frontPost.comments.size());
    	assertEquals(4,Comment.count());
    }
    
    @Test
    public void testTags() {
        // Create a new user and save it
        User bob = new User("bob@gmail.com", "secret", "Bob").save();
     
        // Create a new post
        Post bobPost = new Post(bob, "Jewel post", "Jewel helloworld").save();
        Post anotherBobPost = new Post(bob, "Hop", "Hello world").save();
     
        // Well
        assertEquals(0, Post.findTaggedWith("eye").size());
     
        // Tag it now
        bobPost.tagItWith("jewel").tagItWith("face").save();
        anotherBobPost.tagItWith("jewel").tagItWith("eye").save();
     
        // Check
        assertEquals(2, Post.findTaggedWith("jewel").size());
        assertEquals(1, Post.findTaggedWith("face").size());
        assertEquals(1, Post.findTaggedWith("eye").size());
        assertEquals(1, Post.findTaggedWith("jewel", "face").size());
        assertEquals(1, Post.findTaggedWith("jewel", "eye").size());
        assertEquals(0, Post.findTaggedWith("jewel", "eye", "face").size());
        assertEquals(0, Post.findTaggedWith("eye", "face").size());
        
        List<Map> cloud = Tag.getCloud();
        assertEquals(
            "[{tag=eye, pound=1}, {tag=face, pound=1}, {tag=jewel, pound=2}]",
            cloud.toString()
        );
    }

    
}

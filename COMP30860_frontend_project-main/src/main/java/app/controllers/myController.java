package app.controllers;

import java.io.Console;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import app.FileUploadUtil;
import app.db.Product;
import app.db.User;
import app.db.productRepository;
import app.db.userRepository;
import app.db.orderRepository;
import app.db.Order;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import static app.controllers.Utils.passwordValidation;
import static app.controllers.Utils.checkCardDetails;
import static app.controllers.Utils.logoutUser;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class myController {

    final userRepository userRepository;

    final productRepository productRepository;

    final orderRepository orderRepository;
    User currentUser = new User();
    List<Product> cart = new ArrayList<>();
    boolean emptyCartCheckout = false;

    public myController(userRepository userRepository, productRepository productRepository,
            orderRepository orderRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    private static class warningsClass {
        String userExists = "User with this email exist";
        String userDoesNotExist = "User with this email does not exist";
        String wrongPwdValidation = "Something wrong with password";
        String success = "Success";
        String fail = "Failure";
    }

    private static class statusClass {
        String ordered = "Ordered";
        String paymentSuccess = "Payment made";
        String finished = "Finished";

        public String getNextStatus(String currentStatus) {
            if (currentStatus.equals("Ordered")) {
                return paymentSuccess;
            }
            if (currentStatus.equals("Payment made")) {
                return finished;
            }
            return ordered;
        }
    }

    warningsClass warnings = new warningsClass();
    statusClass orderStatus = new statusClass();

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<Product> getAllProductsToView() {
        List<Product> result = new ArrayList<Product>();
        List<Product> allProduct = productRepository.findAll();

        for (Product product : allProduct) {
            if (product.isVisible()) {
                result.add(product);
            }

        }
        return result;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @PostMapping("/")
    public String addToCart(@RequestParam(name = "productName") String name,
            @RequestParam(name = "productCategory") String category, @RequestParam(name = "productPrice") String price,
            Model model) {

        Product cartProduct = new Product();

        cartProduct.setId(cart.size());
        cartProduct.setName(name);
        cartProduct.setCategory(category);
        cartProduct.setPrice(Double.parseDouble(price));

        cart.add(cartProduct);

        model.addAttribute("myProducts", getAllProductsToView());
        model.addAttribute("amount_in_cart", cart.size());
        model.addAttribute("user", this.currentUser);

        return "index.html";

    }

    @GetMapping("/admin")
    public String admin(Model model) {

        if (currentUser.isAdmin() && currentUser.isLogged()) {
            List<User> totalUsers = getAllUsers();
            List<Product> allProducts = getAllProducts();
            List<Order> allOrders = getAllOrders();

            model.addAttribute("user", currentUser);
            model.addAttribute("myProducts", allProducts);
            model.addAttribute("myUsers", getAllUsers());
            model.addAttribute("total_users", totalUsers.size());
            model.addAttribute("total_products", allProducts.size());
            model.addAttribute("allOrders", allOrders);
            return "admin.html";
        } else {

            return "redirect:/";
        }
    }

    @PostMapping("/admin")
    public String addProduct(@RequestParam(name = "name") String name,
            @RequestParam(name = "category") String category, @RequestParam(name = "price") int price,
            @RequestParam(name = "description") String description, @RequestParam(name = "img") MultipartFile img,
            Model model) throws IOException {

        List<Product> listOfAllProducts = productRepository.findAll();

        int newID = 0;

        if (!listOfAllProducts.isEmpty()) {
            newID = Collections.max(listOfAllProducts).getId() + 1;
        }

        Product registerProduct = new Product();
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(img.getOriginalFilename()));
        registerProduct.setId(newID);

        String pathToImg = "img/productPhotos/" + fileName;
        FileUploadUtil.saveFile("src/main/resources/static/img/productPhotos/", fileName, img);

        registerProduct.setName(name);
        registerProduct.setCategory(category);
        registerProduct.setPrice(price);
        registerProduct.setDescription(description);
        registerProduct.setPathToImg(pathToImg);
        productRepository.save(registerProduct);
        return "redirect:/admin";
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("cart", cart);
        model.addAttribute("myProducts", getAllProductsToView());
        model.addAttribute("amount_in_cart", cart.size());
        model.addAttribute("user", this.currentUser);
        return "index.html";
    }

    @GetMapping("/register")
    public String register() {
        return "register.html";
    }

    @GetMapping("/login")
    public String login() {
        return "login.html";
    }

    @GetMapping("/logout")
    public String logout(Model model) {
        this.currentUser = logoutUser();
        return "redirect:/";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        emptyCartCheckout = false;

        if (cart.isEmpty()) {
            emptyCartCheckout = true;
            return cart(model);
        }

        if (currentUser.isLogged()) {
            Double totalMoneyToPay = cartSumRequest();
            model.addAttribute("user", this.currentUser);
            model.addAttribute("allProductsSumUp", cart);
            model.addAttribute("totalMoneyToPay", totalMoneyToPay);
            return "checkout.html";
        } else {
            return "login.html";
        }
    }

    @PostMapping("/checkout")
    public String checkout(
            @RequestParam(name = "name") String name,
            @RequestParam(name = "number") Long number,
            @RequestParam(name = "CVV") int CVV,
            @RequestParam(name = "expiry-m") int expiry_m,
            @RequestParam(name = "expiry-y") int expiry_y,
            Model model) {

        double cartSum = cartSumRequest();
        model.addAttribute("user", this.currentUser);
        model.addAttribute("allProductsSumUp", cart);
        model.addAttribute("totalMoneyToPay", cartSum);

        if (checkCardDetails(name, number, CVV, expiry_m, expiry_y)) {

            model.addAttribute("check_message", 1);
            model.addAttribute("message", "Your order has been placed");
            // TODO: Refactor this loops
            List<String> cartProductsNames = new ArrayList<>();
            for (Product cartProduct : cart) {
                cartProductsNames.add(cartProduct.getName());
            }

            List<Order> listofOrders = orderRepository.findAll();

            Long newID = (long) 0;

            if (!listofOrders.isEmpty()) {
                newID = Collections.max(listofOrders).getId() + 1;
            }

            Order order = new Order();
            order.setId(newID);
            order.setProductNames(cartProductsNames);
            order.setPrice(cartSumRequest());
            order.setStatus(orderStatus.ordered);
            order.setPaymentId(Double.toString(Math.random()));
            order.setUserName(currentUser.getUsername());

            cart.clear();
            orderRepository.save(order);
            model.addAttribute("check_message", 1);
            model.addAttribute("message", "Your payment was successful");
            List<Order> orders = orderRepository.findByuserName(this.currentUser.getUsername());
            model.addAttribute("orders", orders);
            return "viewOrderByUsername.html";

        } else {
            model.addAttribute("check_message", 1);
            model.addAttribute("message", "Error, please enter the right information");
            return "checkout.html";
        }
    }

    @GetMapping("/changeStatus/{id}")
    public String changeStatus(@PathVariable int id) {
        Order order = orderRepository.findById(id).get(0);
        order.setStatus(orderStatus.getNextStatus(order.getStatus()));
        orderRepository.save(order);
        return "redirect:/admin";

    }

    @GetMapping("/delOrder/{id}")
    public String delOrder(@PathVariable long id) {
        orderRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/delOrderUser/{id}")
    public String delOrderUser(@PathVariable long id) {
        orderRepository.deleteById(id);
        return "redirect:/";
    }

    @GetMapping("/editProduct/{id}")
    public String editProduct(@PathVariable int id,Model model) {
        Product product = productRepository.findById(id).get(0);
        model.addAttribute("product",product);
        return "editProduct.html";
    }

    @PostMapping("/editProduct/{id}")
    public String editProductPost(@RequestParam(name = "name") String name,
            @RequestParam(name = "category") String category, @RequestParam(name = "price") String price,
            @RequestParam(name = "description") String description, @RequestParam(name = "img") MultipartFile img,@PathVariable int id,
            Model model) throws IOException {

        List<Product> listOfAllProducts = productRepository.findAll();

        int newID = 0;

        if (!listOfAllProducts.isEmpty()) {
            newID = Collections.max(listOfAllProducts).getId() + 1;
        }

        Product registerProduct = new Product();
        String fileName = StringUtils.cleanPath(Objects.requireNonNull(img.getOriginalFilename()));
        registerProduct.setId(newID);

        String pathToImg = "img/productPhotos/" + fileName;
        FileUploadUtil.saveFile("src/main/resources/static/img/productPhotos/", fileName, img);

        registerProduct.setName(name);
        registerProduct.setCategory(category);
        registerProduct.setPrice(Double.parseDouble(price));
        registerProduct.setDescription(description);
        registerProduct.setPathToImg(pathToImg);
        productRepository.save(registerProduct);
        productRepository.delete(productRepository.findById(id).get(0));
        model.addAttribute("myProducts", getAllProducts());
        model.addAttribute("total_users", getAllUsers().size());
        model.addAttribute("total_products", getAllProducts().size());
        return "redirect:/admin";
    }

    @GetMapping("/changeVisibilityProduct/{id}")
    public String changeVisibilityProduct(@PathVariable int id) {
        Product product = productRepository.findById(id).get(0);

        product.setVisible(!product.isVisible());
        productRepository.save(product);
        return "redirect:/admin";

    }

    @GetMapping("/changeAdmin/{id}")
    public String changeAdmin(@PathVariable int id) {
        User user = userRepository.findById(id).get(0);

        user.setIsAdmin(!user.isAdmin());
        this.currentUser = user;
        userRepository.save(user);
        return "redirect:/admin";

    }

    @GetMapping("/cart")
    public String cart(Model model) {
        if (cart.isEmpty()) {
            emptyCartCheckout = true;
        } else {
            emptyCartCheckout = false;
        }

        model.addAttribute("emptyCartCheckout", emptyCartCheckout);
        model.addAttribute("size", cart.size());
        model.addAttribute("cart", cart);
        model.addAttribute("user", this.currentUser);
        System.out.print(cartSumRequest());
        model.addAttribute("totalCost", cartSumRequest());

        return "cart.html";
    }

    @PostMapping("/cart")
    public String deleteItem(@RequestParam(name = "productName") String productName,
            @RequestParam(name = "productCategory") String category, @RequestParam(name = "productPrice") String price,
            Model model) {

        for (Product entry : cart) {
            if (entry.getName().equals(productName) && entry.getCategory().equals(category)
                    && entry.getPrice() == Double.parseDouble(price)) {
                cart.remove(entry);
                break;
            }
        }
        return "redirect:/cart";
    }

    public double cartSumRequest() {
        double sum = 0;
        if (cart.isEmpty()) {
            return 0;
        }
        for (int i = 0; i < cart.size(); i++) {
            sum += ((Product) cart.get(i)).getPrice();
        }
        return sum;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam(name = "name") String username,
            @RequestParam(name = "password") String password, Model model) {

        if (userRepository.existsByUsername(username)) {
            model.addAttribute("response", warnings.userExists);
            return "register.html";
        }

        if (!passwordValidation(password)) {
            model.addAttribute("response", warnings.wrongPwdValidation);
            return "register.html";
        }
        User registerUser = new User();
        List<User> listOfAllUsers = getAllUsers();

        int newID = 0;

        if (!listOfAllUsers.isEmpty()) {

            for (User user : listOfAllUsers) {
                int temp = user.getId();

                if (temp > newID) {
                    newID = temp;
                }
            }
            newID += 1;
        }
        registerUser.setId(newID);
        registerUser.setUsername(username);
        registerUser.setPassword(password);
        userRepository.save(registerUser);
        return "login.html";
    }

    @PostMapping("/login")
    public void login(HttpServletRequest request, HttpServletResponse response, Model model)
            throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        // TODO: Refactor this to not use get(0)

        // If user does not exist
        if (!userRepository.existsByUsername(username)) {
            response.sendRedirect("/login");
            model.addAttribute("hasMessage", 1);
            model.addAttribute("message", warnings.userDoesNotExist);
            return;
        } else { // If user exists but

            User user = userRepository.findByUsername(username).get(0);

            // If password is correct
            if (user.getPassword().equals(password)) {
                response.sendRedirect("/");
                model.addAttribute("hasMessage", 0);
                model.addAttribute("message", warnings.success);
                this.currentUser = user;
                return;
            } else { // If password is incorrect
                response.sendRedirect("/login");
                model.addAttribute("hasMessage", 1);
                model.addAttribute("message", warnings.wrongPwdValidation);
                System.out.println(warnings.fail);
                return;
            }
        }
    }

    @GetMapping("/view/{id}")
    public String viewLocations(@PathVariable int id, Model model) {
        model.addAttribute("user", this.currentUser);
        model.addAttribute("product", productRepository.findById(id).get(0));
        return "view.html";
    }

    @GetMapping("/viewOrderByUsername/{username}")
    public String viewLocations(@PathVariable String username, Model model) {
        List<Order> orders = orderRepository.findByuserName(username);
        model.addAttribute("orders", orders);
        return "viewOrderByUsername.html";
    }

}
package com.example.demo.controllers;

import com.example.demo.model.AccountModified;
import com.example.demo.model.Charges;
import com.example.demo.model.LoanAccount;
import com.example.demo.model.Products;
import com.example.demo.model.models.productCreation;
import com.example.demo.services.ChargeServiceImpl;
import com.example.demo.services.LoanAccountService;
import com.example.demo.services.ProductService;
import com.example.demo.services.bps.Bps;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@Log4j2
@RequestMapping("/products")
public class ProductController {


    public  final ProductService productService;
    public final ChargeServiceImpl chargeServiceImpl;
    public final LoanAccountService loanAccountService;
    public final Bps bps;

    public ProductController(ProductService productService, ChargeServiceImpl chargeServiceImpl, LoanAccountService loanAccountService, Bps bps) {
        this.productService = productService;
        this.chargeServiceImpl = chargeServiceImpl;
        this.loanAccountService = loanAccountService;
        this.bps = bps;
    }

    @PostMapping("/create")
    public ResponseEntity<productCreation> createProductModified(@RequestBody productCreation product){
        productCreation products=bps.saveProduct(product);
        return new ResponseEntity<>(products, HttpStatus.CREATED);
    }
    @PostMapping("/createProduct")
    public ResponseEntity<Products> createProduct(@RequestBody Products product){
        Products products=productService.saveProduct(product);
        return new ResponseEntity<>(products, HttpStatus.CREATED);
    }
    @PostMapping("/createCharge")
    public ResponseEntity<Charges> createCharge(@RequestBody Charges charge){
        Charges charges= chargeServiceImpl.createCharge(charge);
        return new ResponseEntity<>(charges, HttpStatus.CREATED);
    }
    @GetMapping("/getAllAccount")
    public ResponseEntity<List<LoanAccount>> getAllAccounts(){
        List<LoanAccount> accounts=loanAccountService.findAll();
        return new ResponseEntity<>(accounts,HttpStatus.OK);
    }
    @GetMapping("/getLoanAccountId{id}")
    public ResponseEntity<List<AccountModified>> getAccountById(@PathVariable String id){
        log.info("Searching loan accounts with customerId {id}",id);
       List<AccountModified> accounts=loanAccountService.findByCustomerId(id);
        return new ResponseEntity<>(accounts,HttpStatus.OK);
    }
    @GetMapping("getChargeByProductId")
    public ResponseEntity<Optional<Charges>> getChargeByProductId(String productId,String ChargeType){
        Optional<Charges> charges= chargeServiceImpl.getChargeByProductIdAndName(productId,ChargeType);
        return new ResponseEntity<>(charges,HttpStatus.OK);
    }
    @GetMapping("/allProducts")
    public ResponseEntity<List<Products>> getAllProducts(){
        List<Products> products=productService.findAllProducts();
        return new ResponseEntity<>(products,HttpStatus.OK);
    }
    @GetMapping("/getProductById{id}")
    public ResponseEntity<Optional<Products>> getById(@PathVariable Long id){
        log.info("Searching Product with id {id}",id);
        Optional<Products> product=productService.findById(id);
        return new ResponseEntity<>(product,HttpStatus.OK);
    }
    @GetMapping("/getProductByCode{code}")
    public ResponseEntity<Optional<Products>> getByCode(@PathVariable String code){
        log.info("Searching Product with code "+code);
        Optional<Products> product=productService.productRepo.getByCode(code);
        log.info("Product found "+product);
        return new ResponseEntity<>(product,HttpStatus.OK);
    }
    @PutMapping("/update")
    public ResponseEntity<Products> updateProduct(@RequestBody Products product){
        log.info("Updating Product ...."+product);
        Products product1=productService.productRepo.save(product);
        return new ResponseEntity<>(product1,HttpStatus.OK);
    }

}



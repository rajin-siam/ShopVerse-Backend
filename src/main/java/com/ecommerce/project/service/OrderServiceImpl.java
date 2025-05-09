package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.*;
import com.ecommerce.project.payload.*;
import com.ecommerce.project.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    CartService cartService;

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponseDTO createOrder(String email, OrderRequestDTO orderRequest){
        Cart cart = cartRepository.findCartByEmail(email);
        if (cart == null) {
            throw new ResourceNotFoundException("Cart", "email", email);
        }


        Address address = addressRepository.findById(orderRequest.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", orderRequest.getAddressId()));

        Order order = new Order();
        order.setEmail(email);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);

        Payment payment = new Payment("Stripe", orderRequest.getPgPaymentId(), orderRequest.getPgStatus(), orderRequest.getPgResponseMessage(), orderRequest.getPgName());
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);

        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems.isEmpty()) {
            throw new APIException("Cart is empty");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);

        cart.getCartItems().forEach(item -> {
            int quantity = item.getQuantity();
            Product product = item.getProduct();

            // Reduce stock quantity
            product.setQuantity(product.getQuantity() - quantity);

            // Save product back to the database
            productRepository.save(product);

            // Remove items from cart
            cartService.deleteProductFromCart(cart.getCartId(), item.getProduct().getProductId());
        });

        OrderDTO orderDTO = modelMapper.map(savedOrder, OrderDTO.class);
        orderItems.forEach(item -> orderDTO.getOrderItems().add(modelMapper.map(item, OrderItemDTO.class)));

        orderDTO.setAddressId(orderRequest.getAddressId());

        return modelMapper.map(orderDTO, OrderResponseDTO.class);

    }

    @Override
    public OrderResponseDTO getOrderByIdAndEmail(Long orderId, String email) {
        Order order = orderRepository.findByOrderIdAndEmail(orderId, email)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return modelMapper.map(order, OrderResponseDTO.class);
    }

    @Override
    public OrderDTO getOrderById(Long orderId){
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        return modelMapper.map(order, OrderDTO.class);
    }

    @Override
    public OrderResponse getAllOrders(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, String orderStatus, String search){
        pageNumber = Math.max(0, pageNumber - 1);
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ?
                Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);
        Page<Order> pageOrders;

        // Fixed search logic with proper conditionals and parameter order
        if (orderStatus != null && !orderStatus.isEmpty() && search != null && !search.isEmpty()) {
            // Both status and search are provided
            try {
                // Try to parse search as a Long for orderId
                Long orderId = Long.parseLong(search);
                pageOrders = orderRepository.findByOrderStatusAndEmailContainingOrOrderStatusAndOrderId(
                        orderStatus, search, orderStatus, orderId, pageDetails);
            } catch (NumberFormatException e) {
                // If search can't be parsed as Long, just use it as email
                pageOrders = orderRepository.findByOrderStatusAndEmailContaining(
                        orderStatus, search, pageDetails);
            }
        } else if (orderStatus != null && !orderStatus.isEmpty()) {
            // Only status is provided
            pageOrders = orderRepository.findByOrderStatus(orderStatus, pageDetails);
        } else if (search != null && !search.isEmpty()) {
            // Only search is provided
            try {
                // Try to parse search as a Long for orderId
                Long orderId = Long.parseLong(search);
                pageOrders = orderRepository.findByEmailContainingOrOrderId(search, orderId, pageDetails);
            } catch (NumberFormatException e) {
                // If search can't be parsed as Long, just use it as email
                pageOrders = orderRepository.findByEmailContaining(search, pageDetails);
            }
        } else {
            // No filters provided
            pageOrders = orderRepository.findAll(pageDetails);
        }

        List<Order> orders = pageOrders.getContent();
        List<OrderDTO> productDTOS = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setContent(productDTOS);
        orderResponse.setPageNumber(pageOrders.getNumber());
        orderResponse.setPageSize(pageOrders.getSize());
        orderResponse.setTotalElements(pageOrders.getTotalElements());
        orderResponse.setTotalPages(pageOrders.getTotalPages());
        orderResponse.setLastPage(pageOrders.isLast());
        return orderResponse;
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, String orderStatus) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));
        System.out.println(orderStatus);
        order.setOrderStatus(orderStatus);
        Order updatedOrder = orderRepository.save(order);

        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderId", orderId));

        Payment payment = order.getPayment();
        if (payment == null) {
            throw new ResourceNotFoundException("Payment", "orderId", orderId);
        }

        payment.setPgStatus(paymentStatus);
        paymentRepository.save(payment);
    }
}
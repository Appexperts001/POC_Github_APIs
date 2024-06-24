//package com.example.demo.service.impl;
//
//
//import com.example.demo.entity.UserEntity;
//import com.example.demo.model.UserModel;
//import com.example.demo.repository.UserRepository;
//import com.example.demo.service.IUserService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.Optional;
//import java.util.stream.Stream;
//
//@Service
//@RequiredArgsConstructor
//public class UserServiceImpl implements IUserService {
//
//
//    private final UserRepository userRepo;
//
//
//    @Override
//    public UserModel createUser(UserModel userModel) {
//
//        UserEntity userDetails=modelToEntity(userModel);
//        UserEntity userDetailsSaved=  userRepo.save(userDetails);
//        return entityToModel(userDetailsSaved);
//    }
//
//    public UserEntity modelToEntity(UserModel userModel){
//        return UserEntity.builder().userName(userModel.getUserName()).id(userModel.getId()).build();
//    }
//
//    public UserModel entityToModel(UserEntity userEntity){
//        return UserModel.builder().userName(userEntity.getUserName()).id(userEntity.getId()).build();
//    }
//
//}

import { CartForm } from './../../model/cart-form';
import { NgForm } from '@angular/forms';
import { MessengerService } from './../service/messenger.service';
import { PostItem } from './../../model/postItem-dto';
import { TokenStorageService } from './../service/token-storage.service';
import { Post } from './../../model/post-dto';
import { PostService } from './../service/post.service';
import { ActivatedRoute } from '@angular/router';
import { Component, OnInit } from '@angular/core';
@Component({
  selector: 'app-buy',
  templateUrl: './buy.component.html',
  styles: [
  ]
})
export class BuyComponent implements OnInit {

  public post:Post = new Post();
  public id:number = 0;
  public owner:boolean = false;
  public cartList:Array<PostItem> = [];
  form: any = {};
  public total:number = 0;
  public cartForm:CartForm = new CartForm();

  constructor(private route: ActivatedRoute,private postService:PostService,private token:TokenStorageService,private msg:MessengerService) {

  
   }

  ngOnInit(): void {
    
    this.msg.getMsg().subscribe(item => {
      this.cartList=item
      
    },
    error => console.log(error))

    this.cartList.forEach(c =>{
      this.total+=c.price
    })
  
  console.log(this.cartList)
  }


  onSubmit(){
    for (let i = 0; i < this.cartList.length; i++) {
      let a = new PostItem();
      a.postId = this.cartList[i].postId;
      a.item = this.cartList[i].item;
      this.cartForm.items.push(a);
    }

    this.postService.comprar(this.cartForm).subscribe(i =>{
      console.log("Comprado")
    },error => console.log(error))
    console.log(this.cartForm)
  }



}

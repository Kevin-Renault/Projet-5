import { Component } from '@angular/core';
import { SlicePipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { subscribe } from 'diagnostics_channel';
import { Topic } from 'src/app/core/models/topic.model';

@Component({
  selector: 'app-topic',
  imports: [FormsModule, SlicePipe],
  templateUrl: './topic.component.html',
  styleUrls: ['./topic.component.scss']
})
export class TopicComponent {

  topics = [
    {
      title: 'Topic 1',
      content: 'Content of Topic 1...',
      subscribed: false
    },
    {
      title: 'Topic 2',
      content: 'Content of Topic 2...',
      subscribed: true
    },
    {
      title: 'Topic 3',
      content: 'Content of Topic 3...',
      subscribed: true
    },
    {
      title: 'Topic 4',
      content: 'Content of Topic 4...',
      subscribed: false
    }
  ];

  public toggleSubscription(topic: any) {
    topic.subscribed = !topic.subscribed;
  }

}

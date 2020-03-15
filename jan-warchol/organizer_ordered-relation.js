const assert = require('assert');
const mudder = require('mudder');
const {getParentInfo} = require('./new-reactive-cache.js');

// Ends of the (open) range of the seq values to use.
const kZeroSeq = '0';
const kMaxSeq = 'z';

function prepareReorder({reactiveDbView, relationName, object, newPPtr,
                        insertPos=null}) {
  let objects = getParentInfo(reactiveDbView, relationName, newPPtr).children;
  if (insertPos === null) {
    insertPos = objects.length;
  }
  assert(insertPos >= 0);
  assert(insertPos <= objects.length);
  const objCurrentIdx = objects.findIndex((o) => o._id === object._id);
  // Inserting the object before itself or before the following object are both
  // the same and don't require any actual action.
  if (objCurrentIdx != -1 &&
      (insertPos == objCurrentIdx ||
       insertPos == objCurrentIdx + 1)) {
    return [];
  }
  // Filter moved object out of objects list.
  // It spares us some tricky corner cases.
  if (objCurrentIdx != -1) {
    objects = objects.filter((o) => o._id !== object._id);
    if (objCurrentIdx < insertPos) {
      --insertPos;
    }
  }
  const modifiedObj = Object.assign({}, object, {
    [relationName]: {
      parentClass: newPPtr.parentClass,
      parentKey: newPPtr.parentKey,
    },
  });
  const seqs = objects.map((t) => t[relationName].seq);
  seqs[-1] = kZeroSeq;
  seqs.push(kMaxSeq);
  const prevSeq = seqs[insertPos-1];
  const nextSeq = seqs[insertPos];

  if (prevSeq != nextSeq) {
    modifiedObj[relationName].seq =
      mudder.base62.mudder(prevSeq, nextSeq, 1)[0];
    return [modifiedObj];
  }

  // Handle duplicated seq.
  const dupedSeq = prevSeq;
  const firstDupIdx = seqs.indexOf(dupedSeq);
  const lastDupIdx = seqs.lastIndexOf(dupedSeq);
  const newSeqsCount = lastDupIdx - firstDupIdx + 2;
  // Note: kZeroSeq and kMaxSeq are never duplicated.
  // TODO: Make distribution non-uniform (leave more space around) and make
  //       sure it encompasses dupedSeq.
  const newSeqs =
    mudder.base62.mudder(seqs[firstDupIdx-1], seqs[lastDupIdx+1], newSeqsCount);
  modifiedObj[relationName].seq = newSeqs[insertPos - firstDupIdx];

  const modifiedObjects = [];

  // Update seqs in objects before the one inserted.
  for (let i = firstDupIdx; i < insertPos; ++i) {
    const obj = Object.assign({}, objects[i]);
    obj[relationName].seq = newSeqs[i - firstDupIdx];
    modifiedObjects.push(obj);
  }

  // Update seqs in objects after the one inserted.
  for (let i = insertPos; i <= lastDupIdx; ++i) {
    const obj = Object.assign({}, objects[i]);
    obj[relationName].seq = newSeqs[i - firstDupIdx + 1];
    modifiedObjects.push(obj);
  }

  modifiedObjects.push(modifiedObj);
  return modifiedObjects;
}

async function reorderTask(
    {relationName, db, reactiveDbView, task, newPPtr, insertPos=null}) {
  // FIXME this is a bad way of dealing with it, but if I try to remove
  // planning relation in a separate operation from subtasking relation, I get
  // update conflicts.
  if (relationName == 'subtaskOf') {
    delete task.plannedFor
  }
  const tasksToSave = prepareReorder({
    reactiveDbView,
    relationName,
    object: task,
    newPPtr,
    insertPos,
  });
  for (let taskToSave of tasksToSave) {
    await db.put(taskToSave);
  }
}

function planTask(options) {
  return reorderTask(Object.assign({}, options, {relationName: 'plannedFor'}));
}

function nestTask(options) {
  return reorderTask(Object.assign({}, options, {relationName: 'subtaskOf'}));
}

module.exports = {
  planTask,
  nestTask,
  reorderTask,
  prepareReorder,
};
